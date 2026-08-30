package com.cityeats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cityeats.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
