package com.group.viewer.controller;

import com.group.common.dto.Result;
import com.group.viewer.service.ViewerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/viewer")
@CrossOrigin(origins = "*")
public class ViewerController {

    @Autowired
    private ViewerService viewerService;

    // 原接口保留 (按ID查)
    @GetMapping("/tags/{viewerId}")
    public Result<List<String>> getViewerTags(@PathVariable Long viewerId) {
        return viewerService.getViewerTags(viewerId);
    }
    
    // 新增接口 (按姓名查)
    @GetMapping("/tags/name")
    public Result<List<String>> getViewerTagsByName(@RequestParam String viewerName) {
        return viewerService.getViewerTagsByName(viewerName);
    }
}
