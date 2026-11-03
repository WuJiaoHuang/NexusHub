package com.nexushub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexushub.entity.UserInfo;
import com.nexushub.mapper.UserInfoMapper;
import com.nexushub.service.IUserInfoService;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {
}
