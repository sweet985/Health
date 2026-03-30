package com.example.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.health.entity.Resource;
import com.example.health.mapper.ResourceMapper;
import com.example.health.service.ResourceService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Collections;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {
    @Override
    public List<Resource> getByType(Integer type) {
        return list(new QueryWrapper<Resource>().eq("type", type));
    }

    @Override
    public List<Resource> getRandomByType(Integer type, int limit) {
        // Fetch all resources of the type, shuffle them, and return the top 'limit'
        List<Resource> all = list(new QueryWrapper<Resource>().eq("type", type));
        Collections.shuffle(all);
        return all.subList(0, Math.min(limit, all.size()));
    }

    @Override
    public List<Resource> searchResources(Integer type, String keyword) {
        QueryWrapper<Resource> queryWrapper = new QueryWrapper<>();
        if (type != null) {
            queryWrapper.eq("type", type);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.like("title", keyword).or().like("content", keyword));
        }
        return list(queryWrapper);
    }
}
