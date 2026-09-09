package com.campuslink.service;

import com.campuslink.dto.Result;
import com.campuslink.dto.ScrollResult;
import com.campuslink.dto.UserDTO;
import com.campuslink.entity.CampusPost;
import com.campuslink.entity.User;
import com.campuslink.mapper.CampusPostMapper;
import com.campuslink.service.impl.CampusPostServiceImpl;
import com.campuslink.utils.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CampusPostFeedServiceTest {

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void queryCampusPostOfFollowShouldReturnScrollCursorWithScoreAndOffset() {
        CampusPostMapper postMapper = mock(CampusPostMapper.class);
        IUserService userService = mock(IUserService.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);
        CampusPostServiceImpl service = new CampusPostServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", postMapper);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "stringRedisTemplate", redisTemplate);

        UserDTO current = new UserDTO();
        current.setId(1L);
        UserHolder.saveUser(current);
        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("10", 1000.0));
        tuples.add(new DefaultTypedTuple<>("11", 900.0));
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeByScoreWithScores(eq("feed:1"), eq(0.0), eq(1000.0), eq(0L), eq(2L)))
                .thenReturn(tuples);
        when(zSetOperations.score(anyString(), eq("1"))).thenReturn(null);
        when(postMapper.selectList(any())).thenReturn(Arrays.asList(
                new CampusPost().setId(10L).setUserId(2L).setTitle("图书馆动态"),
                new CampusPost().setId(11L).setUserId(2L).setTitle("篮球场动态")
        ));
        when(userService.getById(2L)).thenReturn(new User().setId(2L).setNickName("校园用户2").setIcon(""));

        Result response = service.queryCampusPostOfFollow(1000L, 0);

        assertThat(response.getSuccess()).isTrue();
        ScrollResult scroll = (ScrollResult) response.getData();
        assertThat(scroll.getMinTime()).isEqualTo(900L);
        assertThat(scroll.getOffset()).isEqualTo(1);
        List<CampusPost> posts = (List<CampusPost>) scroll.getList();
        assertThat(posts).extracting(CampusPost::getId).containsExactly(10L, 11L);
    }
}
