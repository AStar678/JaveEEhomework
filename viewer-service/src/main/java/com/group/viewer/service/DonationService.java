package com.group.viewer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.group.common.entity.DonationRecord;
import com.group.viewer.entity.Anchor;
import com.group.viewer.entity.Viewer;
import com.group.viewer.mapper.AnchorMapper;
import com.group.viewer.mapper.DonationMapper;
import com.group.viewer.mapper.ViewerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
@Slf4j
public class DonationService {

    @Autowired
    private DonationMapper donationMapper;
    
    @Autowired
    private AnchorMapper anchorMapper;
    
    @Autowired
    private ViewerMapper viewerMapper;

    @Transactional(rollbackFor = Exception.class)
    public void processDonation(DonationRecord record, String traceId) {
        // 0. 校验金额
        if (record.getAmount() == null || record.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("打赏金额必须大于0");
        }

        // 1. 校验并补全主播信息
        if (record.getAnchorId() == null) {
            throw new IllegalArgumentException("主播ID不能为空");
        }
        
        Anchor anchor = anchorMapper.selectById(record.getAnchorId());
        if (anchor == null) {
            throw new IllegalArgumentException("主播不存在: " + record.getAnchorId());
        }
        
        record.setAnchorName(anchor.getName());
        record.setAnchorGender(anchor.getGender());

        // 2. 处理观众信息 (自动注册/查询)
        if (record.getViewerName() == null || record.getViewerName().trim().isEmpty()) {
            throw new IllegalArgumentException("观众姓名不能为空");
        }
        
        // 尝试根据姓名查询观众
        Viewer viewer = viewerMapper.selectOne(new LambdaQueryWrapper<Viewer>()
                .eq(Viewer::getName, record.getViewerName()));
        
        if (viewer == null) {
            // 新观众，自动注册
            viewer = new Viewer();
            viewer.setName(record.getViewerName());
            // 如果前端传了性别就用，没传默认男
            viewer.setGender(record.getViewerGender() != null ? record.getViewerGender() : 1);
            try {
                viewerMapper.insert(viewer);
            } catch (DuplicateKeyException e) {
                // 并发情况下可能重复插入，重新查询一次
                viewer = viewerMapper.selectOne(new LambdaQueryWrapper<Viewer>()
                        .eq(Viewer::getName, record.getViewerName()));
            }
        }
        
        // 补全观众ID和性别(以数据库为准)
        record.setViewerId(viewer.getId());
        record.setViewerGender(viewer.getGender());

        // 3. 填充基础信息
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        record.setTraceId(traceId);

        if (record.getDonateTime() == null) {
            record.setDonateTime(LocalDateTime.now());
        }
        if (record.getSyncStatus() == null) {
            record.setSyncStatus(0);
        }

        try {
            // 4. 插入数据库
            donationMapper.insert(record);
        } catch (DuplicateKeyException e) {
            log.warn("重复打赏请求 (幂等性拦截): traceId={}", traceId);
        }
    }

    public List<DonationRecord> getDonationsAfterId(Long lastId, Integer limit) {
        return donationMapper.selectList(
                new LambdaQueryWrapper<DonationRecord>()
                        .gt(DonationRecord::getId, lastId)
                        .orderByAsc(DonationRecord::getId)
                        .last("LIMIT " + limit)
        );
    }
}
