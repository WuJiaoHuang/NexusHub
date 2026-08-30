package com.cityeats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityeats.dto.Result;
import com.cityeats.entity.Blog;

public interface IBlogService extends IService<Blog> {
    Result saveBlog(Blog blog);
    Result updateLike(Long id);
    Result queryHotBlog(Integer current);
    Result queryBlogById(Long id);
    Result queryBlogLikes(Long id);
    Result quertBlogOfFollow(Long max, Integer offset);
}
