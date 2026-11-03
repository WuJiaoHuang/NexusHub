package com.nexushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexushub.dto.Result;
import com.nexushub.entity.UserInterest;

import java.util.List;

public interface IUserInterestService extends IService<UserInterest> {
    Result updateMyInterests(List<String> tags);
    Result queryMyInterests();
    List<String> listTagsByUserId(Long userId);
}
