package com.cityeats.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityeats.entity.UserInfo;
import com.cityeats.mapper.UserInfoMapper;
import com.cityeats.service.IUserInfoService;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {
}
