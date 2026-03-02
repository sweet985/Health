package com.example.health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.health.entity.Resource;
import java.util.List;

public interface ResourceService extends IService<Resource> {
    List<Resource> getByType(Integer type);
    List<Resource> getRandomByType(Integer type, int limit);
}
