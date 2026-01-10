package com.group.viewer.service;

import com.group.viewer.dto.TopViewerDTO;
import com.group.viewer.entity.Anchor;
import com.group.viewer.mapper.AnchorMapper;
import com.group.viewer.mapper.DonationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TopViewerService {

    @Autowired
    private DonationMapper donationMapper;
    
    @Autowired
    private AnchorMapper anchorMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY = "top_viewer_calculation_lock";

    // 模拟需要计算的主播ID列表，实际情况可能从数据库获取
    private static final Long[] ANCHOR_IDS = {1L, 2L, 3L}; 

    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void calculateTopViewers() {
        // 使用 setIfAbsent (SETNX) 获取锁
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "locked", 50, TimeUnit.SECONDS);
        
        if (Boolean.TRUE.equals(locked)) {
            try {
                log.info("获得分布式锁，开始计算Top观众");
                for (Long anchorId : ANCHOR_IDS) {
                    List<TopViewerDTO> topViewers = donationMapper.getTopViewersByAnchorId(anchorId);
                    // 这里可以将结果缓存到Redis或者打印日志，或者存储到其他地方
                    log.info("主播 {} 的前10名观众: {}", anchorId, topViewers);
                }
            } finally {
                redisTemplate.delete(LOCK_KEY);
                log.info("计算完成，释放分布式锁");
            }
        } else {
            log.info("未获得分布式锁，跳过本次计算");
        }
    }
    
    public List<TopViewerDTO> getTopViewers(Long anchorId) {
        Anchor anchor = anchorMapper.selectById(anchorId);
        if (anchor == null) {
            throw new IllegalArgumentException("主播不存在: " + anchorId);
        }
        return donationMapper.getTopViewersByAnchorId(anchorId);
    }
}
