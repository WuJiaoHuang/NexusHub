package com.campuslink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campuslink.entity.EventCapacity;
import com.campuslink.mapper.EventCapacityMapper;
import com.campuslink.service.IEventCapacityService;
import org.springframework.stereotype.Service;

@Service
public class EventCapacityServiceImpl extends ServiceImpl<EventCapacityMapper, EventCapacity> implements IEventCapacityService {
}
