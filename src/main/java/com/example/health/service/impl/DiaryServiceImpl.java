package com.example.health.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.health.entity.Diary;
import com.example.health.mapper.DiaryMapper;
import com.example.health.service.DiaryService;
import org.springframework.stereotype.Service;

@Service
public class DiaryServiceImpl extends ServiceImpl<DiaryMapper, Diary> implements DiaryService {
}
