package com.group.viewer.controller;

import com.group.common.dto.Result;
import com.group.viewer.dto.TopViewerDTO;
import com.group.viewer.service.TopViewerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/viewer")
@CrossOrigin(origins = "*")
public class TopViewerController {

    @Autowired
    private TopViewerService topViewerService;

    @GetMapping("/top/{anchorId}")
    public Result<List<TopViewerDTO>> getTopViewers(@PathVariable Long anchorId) {
        try {
            return Result.success(topViewerService.getTopViewers(anchorId));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
