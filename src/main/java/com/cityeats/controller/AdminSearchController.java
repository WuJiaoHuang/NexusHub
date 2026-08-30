package com.cityeats.controller;

import com.cityeats.dto.Result;
import com.cityeats.search.ShopSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/admin/search/shop")
public class AdminSearchController {

    @Resource
    private ShopSearchService shopSearchService;

    @PostMapping("/rebuild")
    public Result rebuildShopIndex() {
        try {
            long count = shopSearchService.rebuild();
            return Result.ok(count);
        } catch (Exception e) {
            log.error("商户搜索索引全量重建失败", e);
            return Result.fail("商户搜索索引全量重建失败");
        }
    }
}
