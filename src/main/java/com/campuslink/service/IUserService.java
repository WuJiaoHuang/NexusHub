package com.campuslink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campuslink.dto.LoginFormDTO;
import com.campuslink.dto.Result;
import com.campuslink.dto.UserProfileUpdateDTO;
import com.campuslink.entity.User;

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
