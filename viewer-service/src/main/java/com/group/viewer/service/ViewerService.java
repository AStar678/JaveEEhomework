package com.group.viewer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.group.common.dto.Result;
import com.group.viewer.client.AnalysisClient;
import com.group.viewer.entity.Viewer;
import com.group.viewer.mapper.ViewerMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ViewerService {

    @Autowired
    private AnalysisClient analysisClient;
    
    @Autowired
    private ViewerMapper viewerMapper;

    @CircuitBreaker(name = "analysisService", fallbackMethod = "getViewerTagsFallback")
    public Result<List<String>> getViewerTags(Long viewerId) {
        return analysisClient.getViewerTags(viewerId);
    }
    
    public Result<List<String>> getViewerTagsByName(String viewerName) {
        Viewer viewer = viewerMapper.selectOne(new LambdaQueryWrapper<Viewer>()
                .eq(Viewer::getName, viewerName));
        
        if (viewer == null) {
            return Result.error("观众不存在");
        }
        
        // 复用按ID查询的逻辑 (带熔断)
        return getViewerTags(viewer.getId());
    }

    public Result<List<String>> getViewerTagsFallback(Long viewerId, Throwable t) {
        log.error("Failed to get viewer tags for viewerId: {}, error: {}", viewerId, t.getMessage());
        return Result.success(Collections.singletonList("未查询到个人标签 (服务降级)"));
    }
}
