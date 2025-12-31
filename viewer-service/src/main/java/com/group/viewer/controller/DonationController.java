package com.group.viewer.controller;

import com.group.common.dto.Result;
import com.group.viewer.entity.DonationRecord;
import com.group.viewer.service.DonationService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/viewer")
public class DonationController {

    @Autowired
    private DonationService donationService;

    // 接收打赏请求
    @PostMapping("/donate")
    public Result<String> donate(@RequestBody DonationRecord request) {
        // 从 MDC 获取拦截器生成的 traceId
        String traceId = MDC.get("traceId");

        // 调用业务层
        donationService.processDonation(request, traceId);

        return Result.success("打赏处理成功");
    }
}