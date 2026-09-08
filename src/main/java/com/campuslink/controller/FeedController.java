package com.campuslink.controller;

import com.campuslink.dto.Result;
import com.campuslink.service.ICampusPostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feeds")
public class FeedController {

    @Resource
    private ICampusPostService campusPostService;

    @GetMapping
    public Result queryFollowFeed(@RequestParam("lastId") Long max,
                                  @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return campusPostService.quertCampusPostOfFollow(max, offset);
    }
}
