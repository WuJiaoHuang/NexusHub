package com.nexushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexushub.dto.Result;
import com.nexushub.entity.Post;

public interface IPostService extends IService<Post> {
    Result savePost(Post post);
    Result updateLike(Long id);
    Result queryHotPost(Integer current);
    Result queryPostById(Long id);
    Result queryPostLikes(Long id);
    Result queryPostOfFollow(Long max, Integer offset);
}
