package com.campuslink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campuslink.dto.Result;
import com.campuslink.dto.CampusPlaceSearchRequest;
import com.campuslink.entity.CampusPlace;

public interface ICampusPlaceService extends IService<CampusPlace> {
    Result queryById(Long id);
    Result update(CampusPlace place);
    Result queryCampusPlaceByType(Integer categoryId, Integer current, Double x, Double y);
    Result search(CampusPlaceSearchRequest request);
}
