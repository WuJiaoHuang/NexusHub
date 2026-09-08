package com.campuslink.controller;

import com.campuslink.dto.Result;
import com.campuslink.search.CampusPlaceSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/admin/search/places")
public class AdminSearchController {

    @Resource
    private CampusPlaceSearchService placeSearchService;

    @PostMapping("/rebuild")
    public Result rebuildCampusPlaceIndex() {
        try {
            long count = placeSearchService.rebuild();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("校园地点搜索索引全量重建失败", e);
            return Result.fail("校园地点搜索索引全量重建失败");
        }
    }
}
