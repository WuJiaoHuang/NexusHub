package com.campuslink.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campuslink.dto.Result;
import com.campuslink.dto.UserDTO;
import com.campuslink.entity.UserInterest;
import com.campuslink.mapper.UserInterestMapper;
import com.campuslink.service.IUserInterestService;
import com.campuslink.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserInterestServiceImpl extends ServiceImpl<UserInterestMapper, UserInterest> implements IUserInterestService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateMyInterests(List<String> tags) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        remove(new QueryWrapper<UserInterest>().eq("user_id", user.getId()));
        if (CollUtil.isNotEmpty(tags)) {
            List<UserInterest> interests = tags.stream()
                    .filter(StrUtil::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .limit(20)
                    .map(tag -> new UserInterest()
                            .setUserId(user.getId())
                            .setTag(tag)
                            .setCreateTime(LocalDateTime.now()))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(interests)) {
                saveBatch(interests);
            }
        }
        return Result.ok(listTagsByUserId(user.getId()));
    }

    @Override
    public Result queryMyInterests() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        return Result.ok(listTagsByUserId(user.getId()));
    }

    @Override
    public List<String> listTagsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return query().eq("user_id", userId).list().stream()
                .map(UserInterest::getTag)
                .collect(Collectors.toList());
    }
}
