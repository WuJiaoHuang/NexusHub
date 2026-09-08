package com.campuslink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campuslink.dto.Result;
import com.campuslink.entity.Follow;

public interface IFollowService extends IService<Follow> {
    Result follow(Long followUserId, Boolean isFollow);
    Result isFollow(Long followUserId);
    Result followCommons(Long id);
}
