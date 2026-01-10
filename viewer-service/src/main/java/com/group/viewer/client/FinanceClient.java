package com.group.viewer.client;

import com.group.common.dto.Result;
import com.group.common.entity.DonationRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "finance-service", contextId = "financeClient")
public interface FinanceClient {

    @PostMapping("/finance/sync/donations")
    Result<String> syncDonationRecords(@RequestBody List<DonationRecord> records);
}
