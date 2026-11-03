package com.nexushub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexushub.entity.EventCapacity;
import com.nexushub.mapper.EventCapacityMapper;
import com.nexushub.service.IEventCapacityService;
import org.springframework.stereotype.Service;

@Service
public class EventCapacityServiceImpl extends ServiceImpl<EventCapacityMapper, EventCapacity> implements IEventCapacityService {
}
