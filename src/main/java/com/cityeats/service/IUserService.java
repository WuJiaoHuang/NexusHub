package com.cityeats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityeats.dto.LoginFormDTO;
import com.cityeats.dto.Result;
import com.cityeats.entity.User;

import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {
    Result sendCode(String phone, HttpSession session);
    Result login(LoginFormDTO loginForm, HttpSession session);
    Result logout(String token);
    Result sign();
    Result signCount();
}
