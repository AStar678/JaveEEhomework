package com.group.viewer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.group.common.dto.Result;
import com.group.common.entity.DonationRecord;
import com.group.viewer.mapper.DonationMapper;
import com.group.viewer.service.DonationService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/viewer")
@CrossOrigin(origins = "*")
public class DonationController {

    @Autowired
    private DonationService donationService;
    
    @Autowired
    private DonationMapper donationMapper;

    // 接收打赏请求
    @PostMapping("/donate")
    public Result<String> donate(@RequestBody DonationRecord request) {
        // 从 MDC 获取拦截器生成的 traceId
        String traceId = MDC.get("traceId");

        // 调用业务层
        donationService.processDonation(request, traceId);

        return Result.success("打赏处理成功");
    }

    // 供 Analysis 服务 ETL 使用：批量拉取数据
    @GetMapping("/donations/batch")
    public Result<List<DonationRecord>> getDonationsBatch(
            @RequestParam("lastId") Long lastId,
            @RequestParam(value = "limit", defaultValue = "1000") Integer limit) {
        
        return Result.success(donationService.getDonationsAfterId(lastId, limit));
    }
    
    // 新增：分页查询打赏明细 (供监控端使用)
    @GetMapping("/donations/list")
    public Result<IPage<DonationRecord>> getDonationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<DonationRecord> pageParam = new Page<>(page, size);
        IPage<DonationRecord> result = donationMapper.selectPage(pageParam, 
                new LambdaQueryWrapper<DonationRecord>().orderByDesc(DonationRecord::getId));
        
        return Result.success(result);
    }
}
