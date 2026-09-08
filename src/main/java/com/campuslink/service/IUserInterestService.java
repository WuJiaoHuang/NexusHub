package com.campuslink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campuslink.dto.Result;
import com.campuslink.entity.UserInterest;

import java.util.List;

public interface IUserInterestService extends IService<UserInterest> {
    Result updateMyInterests(List<String> tags);
    Result queryMyInterests();
    List<String> listTagsByUserId(Long userId);
}
