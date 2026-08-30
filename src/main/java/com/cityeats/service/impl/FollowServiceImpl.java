package com.cityeats.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityeats.dto.Result;
import com.cityeats.dto.UserDTO;
import com.cityeats.entity.Follow;
import com.cityeats.entity.User;
import com.cityeats.mapper.FollowMapper;
import com.cityeats.service.IFollowService;
import com.cityeats.service.IUserService;
import com.cityeats.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String key = "follows:" + user.getId();
        if (Boolean.TRUE.equals(isFollow)) {
            Follow follow = new Follow();
            follow.setUserId(user.getId());
            follow.setFollowUserId(followUserId);
            follow.setCreateTime(LocalDateTime.now());
            boolean saved = save(follow);
            if (saved) {
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            remove(query().eq("user_id", user.getId()).eq("follow_user_id", followUserId).getWrapper());
            stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.ok(false);
        }
        Integer count = query().eq("user_id", user.getId()).eq("follow_user_id", followUserId).count();
        return Result.ok(count != null && count > 0);
    }

    @Override
    public Result followCommons(Long id) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect("follows:" + user.getId(), "follows:" + id);
        if (CollUtil.isEmpty(intersect)) {
            return Result.ok(CollUtil.newArrayList());
        }
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> users = userService.listByIds(ids).stream()
                .map(u -> BeanUtil.copyProperties(u, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(users);
    }
}
