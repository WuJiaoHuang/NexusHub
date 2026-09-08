package com.campuslink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campuslink.dto.Result;
import com.campuslink.entity.CampusEvent;

public interface ICampusEventService extends IService<CampusEvent> {
    void addCampusEvent(CampusEvent event);
    Result queryEvents(Integer current);
    Result queryHotEvents(Integer current);
    Result queryCampusEventOfCampusPlace(Long placeId);
}
