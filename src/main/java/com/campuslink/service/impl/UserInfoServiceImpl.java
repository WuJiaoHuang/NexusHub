package com.campuslink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campuslink.entity.UserInfo;
import com.campuslink.mapper.UserInfoMapper;
import com.campuslink.service.IUserInfoService;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {
}
