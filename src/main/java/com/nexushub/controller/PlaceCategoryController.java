package com.nexushub.controller;


import com.nexushub.dto.Result;
import com.nexushub.entity.PlaceCategory;
import com.nexushub.service.IPlaceCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * REST API 控制器
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
