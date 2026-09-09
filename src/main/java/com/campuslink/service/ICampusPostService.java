package com.campuslink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campuslink.dto.Result;
import com.campuslink.entity.CampusPost;

public interface ICampusPostService extends IService<CampusPost> {
    Result saveCampusPost(CampusPost post);
    Result updateLike(Long id);
    Result queryHotCampusPost(Integer current);
    Result queryCampusPostById(Long id);
    Result queryCampusPostLikes(Long id);
    Result queryCampusPostOfFollow(Long max, Integer offset);
}

