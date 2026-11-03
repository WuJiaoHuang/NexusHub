package com.nexushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexushub.dto.Result;
import com.nexushub.entity.Event;

public interface IEventService extends IService<Event> {
    void addEvent(Event event);
    Result queryEvents(Integer current);
    Result queryHotEvents(Integer current);
    Result queryEventOfPlace(Long placeId);
}
