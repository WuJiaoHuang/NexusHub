package com.nexushub.controller;

import com.nexushub.dto.Result;
import com.nexushub.service.IPostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feeds")
public class FeedController {

    @Resource
    private IPostService postService;

    @GetMapping
    public Result queryFollowFeed(@RequestParam("lastId") Long max,
                                  @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return postService.queryPostOfFollow(max, offset);
    }
}

