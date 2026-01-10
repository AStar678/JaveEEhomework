package com.group.viewer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.group.common.dto.Result;
import com.group.viewer.client.FinanceClient;
import com.group.common.entity.DonationRecord;
import com.group.viewer.mapper.DonationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FinanceSyncService {

    @Autowired
    private DonationMapper donationMapper;

    @Autowired
    private FinanceClient financeClient;

    @Scheduled(fixedRate = 30000) // 每30秒同步一次
    // 注意：这里不要加 @Transactional，因为 Feign 调用耗时较长，且如果 Feign 成功但本地更新失败，会导致数据重复推送
    // 我们采用手动控制事务或者仅在更新本地状态时开启事务
    public void syncDonationData() {
        log.info("开始同步打赏数据给财务服务...");

        List<DonationRecord> unsyncedRecords = donationMapper.selectList(
                new LambdaQueryWrapper<DonationRecord>()
                        .eq(DonationRecord::getSyncStatus, 0)
                        .last("LIMIT 100")
        );

        if (unsyncedRecords.isEmpty()) {
            log.info("没有需要同步的数据");
            return;
        }

        try {
            log.info("准备推送 {} 条数据到财务服务", unsyncedRecords.size());
            Result<String> result = financeClient.syncDonationRecords(unsyncedRecords);

            if (result != null && result.getCode() == 200) {
                updateSyncStatus(unsyncedRecords);
                log.info("成功同步 {} 条打赏记录", unsyncedRecords.size());
            } else {
                log.error("财务服务同步失败: {}", (result != null ? result.getMessage() : "返回为空"));
            }
        } catch (Exception e) {
            log.error("同步打赏数据异常", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSyncStatus(List<DonationRecord> records) {
        List<Long> ids = records.stream().map(DonationRecord::getId).collect(Collectors.toList());
        donationMapper.update(null,
                new LambdaUpdateWrapper<DonationRecord>()
                        .in(DonationRecord::getId, ids)
                        .set(DonationRecord::getSyncStatus, 1)
        );
    }
}
