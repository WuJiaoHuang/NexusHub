package com.campuslink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campuslink.dto.Result;
import com.campuslink.dto.ScrollResult;
import com.campuslink.dto.UserDTO;
import com.campuslink.entity.CampusPost;
import com.campuslink.entity.Follow;
import com.campuslink.entity.User;
import com.campuslink.mapper.CampusPostMapper;
import com.campuslink.service.ICampusPostService;
import com.campuslink.service.IFollowService;
import com.campuslink.service.IUserService;
import com.campuslink.utils.RedisConstants;
import com.campuslink.utils.SystemConstants;
import com.campuslink.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CampusPostServiceImpl extends ServiceImpl<CampusPostMapper, CampusPost> implements ICampusPostService {

    @Resource
    private IUserService userService;
    @Resource
    private IFollowService followService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result saveCampusPost(CampusPost post) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        post.setUserId(user.getId());
        post.setLiked(post.getLiked() == null ? 0 : post.getLiked());
        post.setComments(post.getComments() == null ? 0 : post.getComments());
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        save(post);
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        for (Follow follow : follows) {
            stringRedisTemplate.opsForZSet().add(RedisConstants.FEED_KEY + follow.getUserId(), post.getId().toString(), System.currentTimeMillis());
        }
        return Result.ok(post.getId());
    }

    @Override
    public Result updateLike(Long id) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String key = RedisConstants.POST_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, user.getId().toString());
        if (score == null) {
            update().setSql("liked = liked + 1").eq("id", id).update();
            stringRedisTemplate.opsForZSet().add(key, user.getId().toString(), System.currentTimeMillis());
        } else {
            update().setSql("liked = liked - 1").eq("id", id).update();
            stringRedisTemplate.opsForZSet().remove(key, user.getId().toString());
        }
        return Result.ok();
    }

    @Override
    public Result queryHotCampusPost(Integer current) {
        Page<CampusPost> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        page.getRecords().forEach(this::fillCampusPostUserAndLike);
        return Result.ok(page.getRecords());
    }

    @Override
    public Result queryCampusPostById(Long id) {
        CampusPost post = getById(id);
        if (post == null) {
            return Result.fail("校园动态不存在");
        }
        fillCampusPostUserAndLike(post);
        return Result.ok(post);
    }

    @Override
    public Result queryCampusPostLikes(Long id) {
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(RedisConstants.POST_LIKED_KEY + id, 0, 4);
        if (CollUtil.isEmpty(top5)) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<UserDTO> users = userService.query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list()
                .stream().map(user -> BeanUtil.copyProperties(user, UserDTO.class)).collect(Collectors.toList());
        return Result.ok(users);
    }

    @Override
    public Result queryCampusPostOfFollow(Long max, Integer offset) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> typedTuples =
                stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(RedisConstants.FEED_KEY + user.getId(), 0, max, offset, 2);
        if (CollUtil.isEmpty(typedTuples)) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            ids.add(Long.valueOf(tuple.getValue()));
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            } else {
                minTime = time;
                os = 1;
            }
        }
        String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<CampusPost> posts = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        posts.forEach(this::fillCampusPostUserAndLike);
        ScrollResult result = new ScrollResult();
        result.setList(posts);
        result.setMinTime(minTime);
        result.setOffset(os);
        return Result.ok(result);
    }

    private void fillCampusPostUserAndLike(CampusPost post) {
        User user = userService.getById(post.getUserId());
        if (user != null) {
            post.setName(user.getNickName());
            post.setIcon(user.getIcon());
        }
        UserDTO current = UserHolder.getUser();
        if (current == null) {
            post.setIsLike(false);
            return;
        }
        Double score = stringRedisTemplate.opsForZSet().score(RedisConstants.POST_LIKED_KEY + post.getId(), current.getId().toString());
        post.setIsLike(score != null);
    }
}

