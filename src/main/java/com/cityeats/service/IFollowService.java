package com.cityeats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityeats.dto.Result;
import com.cityeats.entity.Follow;

public interface IFollowService extends IService<Follow> {
    Result follow(Long followUserId, Boolean isFollow);
    Result isFollow(Long followUserId);
    Result followCommons(Long id);
}
