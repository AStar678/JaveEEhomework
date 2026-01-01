package com.group.viewer.service;

import com.group.common.exception.GlobalExceptionHandler;
import com.group.viewer.entity.DonationRecord;
import com.group.viewer.mapper.DonationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID; // 1. 记得导入这个包
import cn.hutool.core.util.StrUtil; // 如果你有 hutool

import java.time.LocalDateTime;

@Service
@Slf4j
public class DonationService {

    @Autowired
    private DonationMapper donationMapper;

    @Transactional(rollbackFor = Exception.class)
    public void processDonation(DonationRecord record, String traceId) {


        // 1. 填充基础信息
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
            System.out.println("⚠️ 警告: 拦截器未生效，Service层手动生成了 traceId: " + traceId);
        }

        // 把 ID 填进对象里
        record.setTraceId(traceId);

        // 确保其他必要字段也有值
        if (record.getDonateTime() == null) {
            record.setDonateTime(LocalDateTime.now());
        }
        if (record.getSyncStatus() == null) {
            record.setSyncStatus(0);
        }

        try {
            // 2. 插入数据库
            // 如果 traceId 已存在，数据库会抛出 DuplicateKeyException
            donationMapper.insert(record);
            log.info("打赏入库成功: viewer={} amount={} traceId={}", record.getViewerId(), record.getAmount(), traceId);
        } catch (DuplicateKeyException e) {
            // 3. 捕获唯一索引冲突，视为幂等成功（或者返回特定提示）
            log.warn("重复打赏请求 (幂等性拦截): traceId={}", traceId);
            // 这里不抛出异常，让 Controller 认为处理成功
        }
    }
}