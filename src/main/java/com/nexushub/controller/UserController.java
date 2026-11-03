package com.nexushub.controller;

import com.nexushub.security.PublicEndpoint;

import cn.hutool.core.bean.BeanUtil;
import com.nexushub.common.BusinessException;
import com.nexushub.dto.AvatarUpdateDTO;
import com.nexushub.dto.LoginFormDTO;
import com.nexushub.dto.Result;
import com.nexushub.dto.UserDTO;
import com.nexushub.dto.UserProfileUpdateDTO;
import com.nexushub.entity.User;
import com.nexushub.entity.UserInfo;
import com.nexushub.service.IUserInfoService;
import com.nexushub.service.IUserInterestService;
import com.nexushub.service.IUserService;
import com.nexushub.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping({"/users", "/auth"})
public class UserController {

    @Resource
    private IUserService userService;
    @Resource
    private IUserInfoService userInfoService;
    @Resource
    private IUserInterestService userInterestService;

    @PostMapping("/code")
    @PublicEndpoint
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        return userService.sendCode(phone, session);
    }

    @PostMapping("/login")
    @PublicEndpoint
    public Result login(@Valid @RequestBody LoginFormDTO loginForm, HttpSession session) {
        return userService.login(loginForm, session);
    }

    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        return userService.logout(request.getHeader("authorization"));
    }

    @GetMapping("/me")
    public Result me() {
        UserDTO current = UserHolder.getUser();
        if (current == null) {
            return Result.fail("请先登录");
        }
        User user = userService.getById(current.getId());
        if (user == null) {
            return Result.fail("用户不存在");
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.ok(userDTO);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId) {
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        return Result.ok(info);
    }

    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.ok(userDTO);
    }

    @PutMapping("/avatar")
    public Result updateAvatar(@Valid @RequestBody AvatarUpdateDTO request) {
        try {
            return userService.updateAvatar(request.getUrl());
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public Result updateProfile(@Valid @RequestBody UserProfileUpdateDTO request) {
        return userService.updateProfile(request);
    }

    @PutMapping("/interests")
    public Result updateInterests(@RequestBody List<String> tags) {
        return userInterestService.updateMyInterests(tags);
    }

    @GetMapping("/interests")
    public Result queryInterests() {
        return userInterestService.queryMyInterests();
    }

    @PostMapping("/sign")
    public Result sign() {
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result signCount() {
        return userService.signCount();
    }
}
