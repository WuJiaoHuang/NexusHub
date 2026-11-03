package com.nexushub.controller;

import com.nexushub.dto.Result;
import com.nexushub.search.PlaceSearchService;
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
    private PlaceSearchService placeSearchService;

    @PostMapping("/rebuild")
    public Result rebuildPlaceIndex() {
        try {
            long count = placeSearchService.rebuild();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("地点搜索索引全量重建失败", e);
            return Result.fail("地点搜索索引全量重建失败");
        }
    }
}
