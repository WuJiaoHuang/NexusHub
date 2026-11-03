package com.nexushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexushub.dto.Result;
import com.nexushub.dto.PlaceSearchRequest;
import com.nexushub.entity.Place;

public interface IPlaceService extends IService<Place> {
    Result queryById(Long id);
    Result update(Place place);
    Result queryPlaceByType(Integer categoryId, Integer current, Double x, Double y);
    Result search(PlaceSearchRequest request);
}
