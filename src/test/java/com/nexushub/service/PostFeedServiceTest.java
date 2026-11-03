package com.nexushub.service;

import com.nexushub.dto.Result;
import com.nexushub.dto.ScrollResult;
import com.nexushub.dto.UserDTO;
import com.nexushub.entity.Post;
import com.nexushub.entity.User;
import com.nexushub.mapper.PostMapper;
import com.nexushub.service.impl.PostServiceImpl;
import com.nexushub.utils.UserHolder;
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

class PostFeedServiceTest {

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void queryPostOfFollowShouldReturnScrollCursorWithScoreAndOffset() {
        PostMapper postMapper = mock(PostMapper.class);
        IUserService userService = mock(IUserService.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);
        PostServiceImpl service = new PostServiceImpl();
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
                new Post().setId(10L).setUserId(2L).setTitle("城市书咖动态"),
                new Post().setId(11L).setUserId(2L).setTitle("篮球场动态")
        ));
        when(userService.getById(2L)).thenReturn(new User().setId(2L).setNickName("模拟用户2").setIcon(""));

        Result response = service.queryPostOfFollow(1000L, 0);

        assertThat(response.getSuccess()).isTrue();
        ScrollResult scroll = (ScrollResult) response.getData();
        assertThat(scroll.getMinTime()).isEqualTo(900L);
        assertThat(scroll.getOffset()).isEqualTo(1);
        List<Post> posts = (List<Post>) scroll.getList();
        assertThat(posts).extracting(Post::getId).containsExactly(10L, 11L);
    }
}
