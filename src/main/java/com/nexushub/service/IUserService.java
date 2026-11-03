package com.nexushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexushub.dto.LoginFormDTO;
import com.nexushub.dto.Result;
import com.nexushub.dto.UserProfileUpdateDTO;
import com.nexushub.entity.User;

import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {
    Result sendCode(String phone, HttpSession session);
    Result login(LoginFormDTO loginForm, HttpSession session);
    Result logout(String token);
    Result updateAvatar(String url);
    Result updateProfile(UserProfileUpdateDTO profile);
    Result sign();
    Result signCount();
}
