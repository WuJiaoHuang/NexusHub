package com.nexushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexushub.dto.Result;
import com.nexushub.entity.Follow;

public interface IFollowService extends IService<Follow> {
    Result follow(Long followUserId, Boolean isFollow);
    Result isFollow(Long followUserId);
    Result followCommons(Long id);
}
