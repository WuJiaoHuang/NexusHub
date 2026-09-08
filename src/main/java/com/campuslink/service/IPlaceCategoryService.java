package com.campuslink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campuslink.dto.Result;
import com.campuslink.entity.PlaceCategory;

public interface IPlaceCategoryService extends IService<PlaceCategory> {
    Result querySort();
}
