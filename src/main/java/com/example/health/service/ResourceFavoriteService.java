package com.example.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.health.entity.Resource;
import com.example.health.entity.ResourceFavorite;

import java.util.List;

public interface ResourceFavoriteService extends IService<ResourceFavorite> {
    boolean toggleFavorite(Long userId, Long resourceId);
    List<Long> getUserFavoriteIds(Long userId);
    List<Resource> getUserFavoriteResources(Long userId);
}