package com.group.finance.controller;

import com.group.common.dto.Result;
import com.group.finance.entity.Settlement;
import com.group.finance.entity.SharingRatio;
import com.group.finance.service.FinanceService;
import com.group.common.entity.DonationRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/finance")
@CrossOrigin(origins = "*")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    // 接收 Viewer 服务推送的打赏数据
    @PostMapping("/sync/donations")
    public Result<String> syncDonationRecords(@RequestBody List<DonationRecord> records) {
        financeService.processDonationRecords(records);
        return Result.success("同步成功");
    }

    // 查询主播结算信息
    @GetMapping("/settlement/{anchorId}")
    public Result<Settlement> getSettlement(@PathVariable Long anchorId) {
        return Result.success(financeService.getSettlementInfo(anchorId));
    }

    // 提现接口
    @PostMapping("/withdraw")
    public Result<String> withdraw(@RequestParam Long anchorId, @RequestParam BigDecimal amount) {
        return financeService.withdraw(anchorId, amount);
    }
    
    // 新增：查询分成比例
    @GetMapping("/ratio/{anchorId}")
    public Result<SharingRatio> getSharingRatio(@PathVariable Long anchorId) {
        return financeService.getSharingRatio(anchorId);
    }
    
    // 新增：修改分成比例
    @PostMapping("/ratio")
    public Result<String> updateSharingRatio(@RequestBody SharingRatio ratio) {
        return financeService.updateSharingRatio(ratio);
    }
}
