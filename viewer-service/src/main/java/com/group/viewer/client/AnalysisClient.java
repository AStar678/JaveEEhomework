package com.group.viewer.client;

import com.group.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "analysis-service", contextId = "analysisClient")
public interface AnalysisClient {

    @GetMapping("/analysis/tags/{viewerId}")
    Result<List<String>> getViewerTags(@PathVariable("viewerId") Long viewerId);
}
