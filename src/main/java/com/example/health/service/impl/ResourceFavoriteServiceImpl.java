package com.example.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.health.entity.Resource;
import com.example.health.entity.ResourceFavorite;
import com.example.health.mapper.ResourceFavoriteMapper;
import com.example.health.service.ResourceFavoriteService;
import com.example.health.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceFavoriteServiceImpl extends ServiceImpl<ResourceFavoriteMapper, ResourceFavorite> implements ResourceFavoriteService {

    @Autowired
    private ResourceService resourceService;

    @Override
    public boolean toggleFavorite(Long userId, Long resourceId) {
        QueryWrapper<ResourceFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("resource_id", resourceId);
        
        ResourceFavorite existing = this.getOne(queryWrapper);
        if (existing != null) {
            // Already favorited, remove it
            this.removeById(existing.getId());
            return false;
        } else {
            // Not favorited, add it
            ResourceFavorite favorite = new ResourceFavorite();
            favorite.setUserId(userId);
            favorite.setResourceId(resourceId);
            this.save(favorite);
            return true;
        }
    }

    @Override
    public List<Long> getUserFavoriteIds(Long userId) {
        QueryWrapper<ResourceFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).select("resource_id");
        
        return this.list(queryWrapper).stream()
                .map(ResourceFavorite::getResourceId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> getUserFavoriteResources(Long userId) {
        List<Long> favoriteIds = getUserFavoriteIds(userId);
        if (favoriteIds.isEmpty()) {
            return List.of();
        }
        
        QueryWrapper<Resource> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", favoriteIds);
        return resourceService.list(queryWrapper);
    }
}