package com.group.analysis.client;

import com.group.common.dto.Result;
import com.group.common.entity.DonationRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "viewer-service", contextId = "viewerClient")
public interface ViewerClient {

    @GetMapping("/viewer/donations/batch")
    Result<List<DonationRecord>> getDonationsBatch(
            @RequestParam("lastId") Long lastId,
            @RequestParam("limit") Integer limit);
}
