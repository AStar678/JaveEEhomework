package com.group.analysis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.group.analysis.entity.ViewerProfile;
import com.group.analysis.mapper.ViewerProfileMapper;
import com.group.analysis.service.ViewerProfileService;
import org.springframework.stereotype.Service;

@Service
public class ViewerProfileServiceImpl extends ServiceImpl<ViewerProfileMapper, ViewerProfile> implements ViewerProfileService {
}
