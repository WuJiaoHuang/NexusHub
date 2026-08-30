package com.cityeats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cityeats.entity.Blog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
}
