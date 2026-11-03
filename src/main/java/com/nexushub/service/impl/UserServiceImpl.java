package com.nexushub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexushub.dto.LoginFormDTO;
import com.nexushub.dto.Result;
import com.nexushub.dto.UserDTO;
import com.nexushub.dto.UserProfileUpdateDTO;
import com.nexushub.entity.User;
import com.nexushub.entity.UserInfo;
import com.nexushub.mapper.UserMapper;
import com.nexushub.service.AvatarUrlValidator;
import com.nexushub.service.IUserInfoService;
import com.nexushub.service.IUserService;
import com.nexushub.utils.RedisConstants;
import com.nexushub.utils.RegexUtils;
import com.nexushub.utils.SystemConstants;
import com.nexushub.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AvatarUrlValidator avatarUrlValidator;
    @Resource
    private IUserInfoService userInfoService;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.info("发送登录验证码，phone={}", phone);
        return Result.ok(code);
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        if (loginForm == null || RegexUtils.isPhoneInvalid(loginForm.getPhone())) {
            return Result.fail("手机号格式错误");
        }
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + loginForm.getPhone());
        if (StrUtil.isBlank(cacheCode) || !cacheCode.equals(loginForm.getCode())) {
            return Result.fail("验证码错误");
        }
        User user = query().eq("phone", loginForm.getPhone()).one();
        if (user == null) {
            user = createUserWithPhone(loginForm.getPhone());
        }
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                cn.hutool.core.bean.copier.CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY + token, userMap);
        stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + loginForm.getPhone());
        return Result.ok(token);
    }

    @Override
    public Result logout(String token) {
        if (StrUtil.isNotBlank(token)) {
            stringRedisTemplate.delete(RedisConstants.LOGIN_USER_KEY + token);
        }
        UserHolder.removeUser();
        return Result.ok();
    }

    @Override
    public Result updateAvatar(String url) {
        UserDTO current = UserHolder.getUser();
        if (current == null) {
            return Result.fail("请先登录");
        }
        avatarUrlValidator.validateForUser(url, current.getId());
        User user = getById(current.getId());
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setIcon(url);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
        UserDTO dto = BeanUtil.copyProperties(user, UserDTO.class);
        UserHolder.saveUser(dto);
        return Result.ok(dto);
    }

    @Override
    public Result updateProfile(UserProfileUpdateDTO profile) {
        UserDTO current = UserHolder.getUser();
        if (current == null) {
            return Result.fail("请先登录");
        }
        User user = getById(current.getId());
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setNickName(profile.getNickName().trim());
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);

        UserInfo info = userInfoService.getById(current.getId());
        if (info == null) {
            info = new UserInfo();
            info.setUserId(current.getId());
            info.setIntroduce(profile.getIntroduce());
            info.setCreateTime(LocalDateTime.now());
            info.setUpdateTime(LocalDateTime.now());
            userInfoService.save(info);
        } else {
            info.setIntroduce(profile.getIntroduce());
            info.setUpdateTime(LocalDateTime.now());
            userInfoService.updateById(info);
        }
        UserDTO dto = BeanUtil.copyProperties(user, UserDTO.class);
        UserHolder.saveUser(dto);
        return Result.ok(dto);
    }

    @Override
    public Result sign() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        LocalDateTime now = LocalDateTime.now();
        String key = RedisConstants.SIGN_KEY + user.getId() + ":" + now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        stringRedisTemplate.opsForValue().setBit(key, now.getDayOfMonth() - 1, true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        LocalDateTime now = LocalDateTime.now();
        String key = RedisConstants.SIGN_KEY + user.getId() + ":" + now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        int day = now.getDayOfMonth();
        List<Long> result = stringRedisTemplate.opsForValue().bitField(key,
                BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(day)).valueAt(0));
        if (result == null || result.isEmpty() || result.get(0) == null) {
            return Result.ok(0);
        }
        long num = result.get(0);
        int count = 0;
        for (int i = 0; i < day; i++) {
            if ((num & 1) == 0) {
                break;
            }
            count++;
            num >>>= 1;
        }
        return Result.ok(count);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);
        return user;
    }
}
