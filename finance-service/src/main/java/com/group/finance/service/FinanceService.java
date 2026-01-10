package com.group.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.group.common.dto.Result;
import com.group.finance.entity.Settlement;
import com.group.finance.entity.SharingRatio;
import com.group.finance.mapper.SettlementMapper;
import com.group.finance.mapper.SharingRatioMapper;
import com.group.common.entity.DonationRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FinanceService {

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private SharingRatioMapper sharingRatioMapper;

    // 处理同步过来的打赏记录
    @Transactional(rollbackFor = Exception.class)
    public void processDonationRecords(List<DonationRecord> records) {
        if (records == null || records.isEmpty()) return;

        // 1. 按主播分组统计总金额
        Map<Long, List<DonationRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(DonationRecord::getAnchorId));

        // 2. 遍历每个主播进行结算
        for (Map.Entry<Long, List<DonationRecord>> entry : grouped.entrySet()) {
            Long anchorId = entry.getKey();
            List<DonationRecord> anchorRecords = entry.getValue();
            
            BigDecimal totalAmount = anchorRecords.stream()
                    .map(DonationRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            String currentAnchorName = anchorRecords.get(0).getAnchorName();

            // 获取分成比例
            SharingRatio ratio = sharingRatioMapper.selectById(anchorId);
            BigDecimal rate;
            String anchorName;

            if (ratio != null) {
                rate = ratio.getRatio();
                anchorName = ratio.getAnchorName();
            } else {
                // 如果没有配置，自动初始化一条默认配置
                rate = new BigDecimal("0.50");
                anchorName = currentAnchorName;
                
                SharingRatio newRatio = new SharingRatio();
                newRatio.setAnchorId(anchorId);
                newRatio.setAnchorName(anchorName);
                newRatio.setRatio(rate);
                sharingRatioMapper.insert(newRatio);
            }

            // 计算结算金额
            BigDecimal settledAmount = totalAmount.multiply(rate);

            // 更新或插入结算表
            Settlement settlement = settlementMapper.selectById(anchorId);
            if (settlement == null) {
                settlement = new Settlement();
                settlement.setAnchorId(anchorId);
                settlement.setAnchorName(anchorName);
                settlement.setTotalRevenue(totalAmount); // 初始化总流水
                settlement.setTotalSettledAmount(settledAmount);
                settlement.setTotalWithdrawnAmount(BigDecimal.ZERO);
                settlementMapper.insert(settlement);
            } else {
                // 累加总流水
                BigDecimal currentRevenue = settlement.getTotalRevenue() != null ? settlement.getTotalRevenue() : BigDecimal.ZERO;
                settlement.setTotalRevenue(currentRevenue.add(totalAmount));
                
                settlement.setTotalSettledAmount(settlement.getTotalSettledAmount().add(settledAmount));
                settlementMapper.updateById(settlement);
            }
        }
        log.info("已处理 {} 条打赏记录的财务结算", records.size());
    }

    // 查询结算信息
    public Settlement getSettlementInfo(Long anchorId) {
        Settlement settlement = settlementMapper.selectById(anchorId);
        if (settlement == null) {
            settlement = new Settlement();
            settlement.setAnchorId(anchorId);
            settlement.setTotalRevenue(BigDecimal.ZERO);
            settlement.setTotalSettledAmount(BigDecimal.ZERO);
            settlement.setTotalWithdrawnAmount(BigDecimal.ZERO);
        }
        return settlement;
    }

    // 提取金额
    @Transactional(rollbackFor = Exception.class)
    public Result<String> withdraw(Long anchorId, BigDecimal amount) {
        Settlement settlement = settlementMapper.selectById(anchorId);
        if (settlement == null) {
            return Result.error("未找到结算账户");
        }

        BigDecimal available = settlement.getTotalSettledAmount().subtract(settlement.getTotalWithdrawnAmount());
        if (available.compareTo(amount) < 0) {
            return Result.error("余额不足，可提现金额: " + available);
        }

        settlement.setTotalWithdrawnAmount(settlement.getTotalWithdrawnAmount().add(amount));
        settlementMapper.updateById(settlement);
        
        return Result.success("提现成功");
    }
    
    // 查询分成比例
    public Result<SharingRatio> getSharingRatio(Long anchorId) {
        SharingRatio ratio = sharingRatioMapper.selectById(anchorId);
        if (ratio == null) {
            return Result.error("未找到分成配置");
        }
        return Result.success(ratio);
    }
    
    // 修改分成比例，并重新计算该主播的累计分成
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateSharingRatio(SharingRatio ratio) {
        if (ratio.getAnchorId() == null || ratio.getRatio() == null) {
            return Result.error("参数不完整");
        }
        if (ratio.getRatio().compareTo(BigDecimal.ZERO) < 0 || ratio.getRatio().compareTo(BigDecimal.ONE) > 0) {
            return Result.error("分成比例必须在 0 到 1 之间");
        }
        
        // 1. 更新配置表
        SharingRatio existing = sharingRatioMapper.selectById(ratio.getAnchorId());
        if (existing == null) {
            sharingRatioMapper.insert(ratio);
        } else {
            existing.setRatio(ratio.getRatio());
            if (ratio.getAnchorName() != null) {
                existing.setAnchorName(ratio.getAnchorName());
            }
            sharingRatioMapper.updateById(existing);
        }
        
        // 2. 重新计算结算表 (基于总流水)
        Settlement settlement = settlementMapper.selectById(ratio.getAnchorId());
        if (settlement != null && settlement.getTotalRevenue() != null) {
            // 新的累计分成 = 总流水 * 新比例
            BigDecimal newSettledAmount = settlement.getTotalRevenue().multiply(ratio.getRatio());
            settlement.setTotalSettledAmount(newSettledAmount);
            settlementMapper.updateById(settlement);
            return Result.success("分成比例更新成功，已重新计算历史分成");
        }
        
        return Result.success("分成比例更新成功");
    }
}
