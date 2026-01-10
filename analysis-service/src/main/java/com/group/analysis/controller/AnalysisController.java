package com.group.analysis.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.group.analysis.entity.HourlyStats;
import com.group.analysis.entity.ViewerProfile;
import com.group.analysis.mapper.HourlyStatsMapper;
import com.group.analysis.mapper.ViewerProfileMapper;
import com.group.common.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analysis")
@CrossOrigin(origins = "*") // 允许跨域
public class AnalysisController {

    @Autowired
    private HourlyStatsMapper hourlyStatsMapper;
    
    @Autowired
    private ViewerProfileMapper viewerProfileMapper;

    // 供 Viewer 服务调用
    @GetMapping("/tags/{viewerId}")
    public Result<List<String>> getViewerTags(@PathVariable Long viewerId) {
        // 模拟计算耗时，测试超时降级
        try {
            long sleepTime = (long) (Math.random() * 500); // 减少睡眠时间，避免频繁超时
            java.util.concurrent.TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewerProfile profile = viewerProfileMapper.selectById(viewerId);
        if (profile != null && profile.getTagLabel() != null) {
            return Result.success(Collections.singletonList(profile.getTagLabel()));
        }
        return Result.success(Collections.singletonList("普通观众"));
    }
    
    // 4.1 按小时、性别、主播查询 (支持动态条件 + 分页)
    @GetMapping("/stats/hourly")
    public Result<IPage<HourlyStats>> getHourlyStats(
            @RequestParam(required = false) Long anchorId,
            @RequestParam(required = false) Integer viewerGender,
            @RequestParam(required = false) Integer anchorGender,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
            
        LambdaQueryWrapper<HourlyStats> query = new LambdaQueryWrapper<>();
        
        if (anchorId != null) {
            query.eq(HourlyStats::getAnchorId, anchorId);
        }
        if (viewerGender != null) {
            query.eq(HourlyStats::getViewerGender, viewerGender);
        }
        if (anchorGender != null) {
            query.eq(HourlyStats::getAnchorGender, anchorGender);
        }
        if (startTime != null) {
            query.ge(HourlyStats::getStatHour, startTime);
        }
        if (endTime != null) {
            query.le(HourlyStats::getStatHour, endTime);
        }
        
        query.orderByAsc(HourlyStats::getStatHour);
        
        Page<HourlyStats> pageParam = new Page<>(page, size);
        IPage<HourlyStats> resultPage = hourlyStatsMapper.selectPage(pageParam, query);
                
        return Result.success(resultPage);
    }
    
    // 新增：查询趋势图数据 (聚合后返回，不分页)
    @GetMapping("/stats/trend")
    public Result<List<Map<String, Object>>> getTrendStats(
            @RequestParam(required = false) Long anchorId,
            @RequestParam(required = false) Integer viewerGender,
            @RequestParam(required = false) Integer anchorGender,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
            
        QueryWrapper<HourlyStats> query = new QueryWrapper<>();
        query.select("stat_hour as time", "SUM(total_amount) as amount");
        
        if (anchorId != null) query.eq("anchor_id", anchorId);
        if (viewerGender != null) query.eq("viewer_gender", viewerGender);
        if (anchorGender != null) query.eq("anchor_gender", anchorGender);
        if (startTime != null) query.ge("stat_hour", startTime);
        if (endTime != null) query.le("stat_hour", endTime);
        
        query.groupBy("stat_hour");
        query.orderByAsc("stat_hour");
        
        List<Map<String, Object>> list = hourlyStatsMapper.selectMaps(query);
        return Result.success(list);
    }
    
    // 新增：查询观众画像分布 (用于饼图)
    @GetMapping("/stats/profile-distribution")
    public Result<List<Map<String, Object>>> getProfileDistribution() {
        List<Map<String, Object>> list = viewerProfileMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ViewerProfile>()
                        .select("tag_label as name", "count(*) as value")
                        .groupBy("tag_label")
        );
        return Result.success(list);
    }
}
