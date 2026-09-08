package com.campuslink.controller;


import com.campuslink.dto.Result;
import com.campuslink.entity.PlaceCategory;
import com.campuslink.service.IPlaceCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/place-categories")
public class PlaceCategoryController {
    @Resource
    private IPlaceCategoryService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
//        List<PlaceCategory> typeList = typeService
//                .query().orderByAsc("sort").list();

        return typeService.querySort();
    }
}
