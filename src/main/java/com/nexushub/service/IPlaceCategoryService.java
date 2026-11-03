package com.nexushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexushub.dto.Result;
import com.nexushub.entity.PlaceCategory;

public interface IPlaceCategoryService extends IService<PlaceCategory> {
    Result querySort();
}
