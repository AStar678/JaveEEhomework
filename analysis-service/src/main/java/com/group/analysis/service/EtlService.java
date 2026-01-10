package com.group.analysis.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.group.analysis.client.ViewerClient;
import com.group.analysis.entity.EtlProgress;
import com.group.analysis.entity.HourlyStats;
import com.group.analysis.entity.ViewerProfile;
import com.group.analysis.mapper.EtlProgressMapper;
import com.group.analysis.mapper.HourlyStatsMapper;
import com.group.analysis.mapper.ViewerProfileMapper;
import com.group.common.dto.Result;
import com.group.common.entity.DonationRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EtlService {

    @Autowired
    private ViewerClient viewerClient;

    @Autowired
    private EtlProgressMapper etlProgressMapper;

    @Autowired
    private HourlyStatsMapper hourlyStatsMapper;
    
    @Autowired
    private ViewerProfileMapper viewerProfileMapper;
    
    @Autowired
    private ViewerProfileService viewerProfileService;
    
    @Value("${analysis.etl.stats-granularity:HOURS}")
    private String statsGranularity;

    private static final String TASK_HOURLY = "HOURLY_STATS";
    private static final int BATCH_SIZE = 1000;

    @Scheduled(fixedDelay = 60000)
    public void runHourlyStatsEtl() {
        log.info("开始执行报表 ETL (粒度: {})...", statsGranularity);
        
        EtlProgress progress = etlProgressMapper.selectById(TASK_HOURLY);
        if (progress == null) {
            progress = new EtlProgress();
            progress.setTaskName(TASK_HOURLY);
            progress.setLastProcessedId(0L);
            etlProgressMapper.insert(progress);
        }

        long lastId = progress.getLastProcessedId();
        boolean hasMore = true;

        while (hasMore) {
            try {
                Result<List<DonationRecord>> result = viewerClient.getDonationsBatch(lastId, BATCH_SIZE);
                if (result == null || result.getData() == null || result.getData().isEmpty()) {
                    hasMore = false;
                    break;
                }

                List<DonationRecord> records = result.getData();
                
                processHourlyStats(records);
                processViewerProfile(records); 

                lastId = records.get(records.size() - 1).getId();
                progress.setLastProcessedId(lastId);
                progress.setLastProcessedTime(LocalDateTime.now());
                etlProgressMapper.updateById(progress);
                
                log.info("已处理到 ID: {}", lastId);

                if (records.size() < BATCH_SIZE) {
                    hasMore = false;
                }
            } catch (Exception e) {
                log.error("ETL 任务异常", e);
                hasMore = false; 
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void processHourlyStats(List<DonationRecord> records) {
        // 确定聚合粒度
        ChronoUnit unit = "MINUTES".equalsIgnoreCase(statsGranularity) ? ChronoUnit.MINUTES : ChronoUnit.HOURS;

        Map<String, List<DonationRecord>> grouped = records.stream().collect(Collectors.groupingBy(r -> {
            LocalDateTime time = r.getDonateTime().truncatedTo(unit);
            return time + "_" + r.getAnchorId() + "_" + r.getViewerGender() + "_" + r.getAnchorGender();
        }));

        for (List<DonationRecord> group : grouped.values()) {
            DonationRecord first = group.get(0);
            LocalDateTime statTime = first.getDonateTime().truncatedTo(unit);
            Long anchorId = first.getAnchorId();
            Integer viewerGender = first.getViewerGender();
            Integer anchorGender = first.getAnchorGender();
            
            BigDecimal total = group.stream()
                    .map(DonationRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            HourlyStats stats = hourlyStatsMapper.selectOne(new LambdaQueryWrapper<HourlyStats>()
                    .eq(HourlyStats::getStatHour, statTime)
                    .eq(HourlyStats::getAnchorId, anchorId)
                    .eq(HourlyStats::getViewerGender, viewerGender)
                    .eq(HourlyStats::getAnchorGender, anchorGender));

            if (stats == null) {
                stats = new HourlyStats();
                stats.setStatHour(statTime);
                stats.setAnchorId(anchorId);
                stats.setAnchorName(first.getAnchorName());
                stats.setAnchorGender(anchorGender);
                stats.setViewerGender(viewerGender);
                stats.setTotalAmount(total);
                hourlyStatsMapper.insert(stats);
            } else {
                stats.setTotalAmount(stats.getTotalAmount().add(total));
                hourlyStatsMapper.updateById(stats);
            }
        }
    }
    
    public void processViewerProfile(List<DonationRecord> records) {
        Map<Long, List<DonationRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(DonationRecord::getViewerId));
        
        int newUsers = 0;
        int updatedUsers = 0;

        for (Map.Entry<Long, List<DonationRecord>> entry : grouped.entrySet()) {
            Long viewerId = entry.getKey();
            List<DonationRecord> viewerRecords = entry.getValue();
            BigDecimal total = viewerRecords.stream()
                    .map(DonationRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            String viewerName = viewerRecords.get(0).getViewerName();
            
            ViewerProfile profile = viewerProfileMapper.selectById(viewerId);
            if (profile == null) {
                profile = new ViewerProfile();
                profile.setViewerId(viewerId);
                profile.setViewerName(viewerName);
                profile.setTotalAmount(total);
                profile.setPercentile(BigDecimal.ZERO);
                profile.setUpdateTime(LocalDateTime.now());
                viewerProfileMapper.insert(profile);
                newUsers++;
            } else {
                profile.setTotalAmount(profile.getTotalAmount().add(total));
                profile.setUpdateTime(LocalDateTime.now());
                viewerProfileMapper.updateById(profile);
                updatedUsers++;
            }
        }
        if (newUsers > 0 || updatedUsers > 0) {
            log.info("画像基础数据更新: 新增 {} 人, 更新 {} 人", newUsers, updatedUsers);
        }
    }
    
    @Scheduled(fixedRateString = "${analysis.etl.profile-calc-rate:60000}") 
    public void runUserProfileCalc() {
        log.info("开始计算用户画像分位数...");
        
        List<ViewerProfile> allProfiles = viewerProfileMapper.selectList(
                new LambdaQueryWrapper<ViewerProfile>().orderByAsc(ViewerProfile::getTotalAmount)
        );
        
        if (allProfiles.isEmpty()) {
            log.info("没有用户画像数据需要计算");
            return;
        }
        
        int totalCount = allProfiles.size();
        log.info("查找到 {} 个用户，开始计算...", totalCount);
        
        List<ViewerProfile> updateList = new ArrayList<>();
        
        for (int i = 0; i < totalCount; i++) {
            ViewerProfile profile = allProfiles.get(i);
            
            BigDecimal rank = new BigDecimal(i + 1);
            BigDecimal total = new BigDecimal(totalCount);
            BigDecimal percentile = rank.divide(total, 4, RoundingMode.HALF_UP);
            
            profile.setPercentile(percentile);
            
            if (percentile.compareTo(new BigDecimal("0.80")) >= 0) {
                profile.setTagLabel("High Spender");
            } else if (percentile.compareTo(new BigDecimal("0.20")) >= 0) {
                profile.setTagLabel("Medium Spender");
            } else {
                profile.setTagLabel("Low Spender");
            }
            
            updateList.add(profile);
            
            if (updateList.size() >= 1000) {
                viewerProfileService.updateBatchById(updateList);
                updateList.clear();
            }
        }
        
        if (!updateList.isEmpty()) {
            viewerProfileService.updateBatchById(updateList);
        }
        
        log.info("用户画像计算完成，更新了 {} 个用户", totalCount);
    }
}
