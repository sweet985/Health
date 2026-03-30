package com.example.health.controller;

import com.example.health.common.CommonResult;
import com.example.health.entity.Resource;
import com.example.health.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/healing")
public class HealingController {

    @Autowired
    private ResourceService resourceService;

    @GetMapping("/list/{type}")
    public CommonResult<List<Resource>> getResources(@PathVariable Integer type) {
        System.out.println("Fetching resources for type: " + type);
        List<Resource> resources = resourceService.getByType(type);
        System.out.println("Found " + resources.size() + " resources");
        return CommonResult.success(resources);
    }

    @GetMapping("/random/{type}")
    public CommonResult<List<Resource>> getRandomResources(@PathVariable Integer type, @RequestParam(defaultValue = "6") int limit) {
        System.out.println("Fetching random resources for type: " + type);
        List<Resource> resources = resourceService.getRandomByType(type, limit);
        return CommonResult.success(resources);
    }

    @GetMapping("/search")
    public CommonResult<List<Resource>> searchResources(
            @RequestParam(required = false) Integer type,
            @RequestParam String keyword) {
        System.out.println("Searching resources for type: " + type + ", keyword: " + keyword);
        List<Resource> resources = resourceService.searchResources(type, keyword);
        return CommonResult.success(resources);
    }
}
