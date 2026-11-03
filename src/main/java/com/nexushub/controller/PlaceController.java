package com.nexushub.controller;

import com.nexushub.security.PublicEndpoint;
import com.nexushub.security.AdminOnly;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexushub.dto.Result;
import com.nexushub.dto.PlaceSearchRequest;
import com.nexushub.entity.Place;
import com.nexushub.service.IPlaceService;
import com.nexushub.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * REST API 控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/places")
public class PlaceController {

    @Resource
    public IPlaceService placeService;

    /**
     * 根据id查询地点信息
     *
     * @param id 地点id
     * @return 地点详情数据
     */
    @GetMapping("/{id}")
    @PublicEndpoint
    public Result queryPlaceById(@PathVariable("id") Long id) throws InterruptedException {
        return placeService.queryById(id);
    }

    /**
     * 新增地点信息
     * @param place 地点数据
     * @return 地点id
     */
    @PostMapping
    @AdminOnly
    public Result savePlace(@RequestBody Place place) {
        // 写入数据库
        placeService.save(place);
        // 返回地点id
        return Result.ok(place.getId());
    }

    /**
     * 更新地点信息
     * @param place 地点数据
     * @return 无
     */
    @PutMapping
    @AdminOnly
    public Result updatePlace(@RequestBody Place place) {
        // 写入数据库

        return placeService.update(place);
    }

    /**
     * 根据地点类型分页查询地点信息
     * @param categoryId 地点类型
     * @param current 页码
     * @return 地点列表
     */
    @GetMapping("/nearby")
    @PublicEndpoint
    public Result queryPlaceByType(
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "x",required = false) Double x,
            @RequestParam(value = "y",required = false) Double y
    ) {
        return placeService.queryPlaceByType(categoryId,current,x,y);
    }

    @GetMapping("/search")
    @PublicEndpoint
    public Result search(PlaceSearchRequest request) {
        return placeService.search(request);
    }

    /**
     * 根据地点名称关键字分页查询地点信息
     * @param name 地点名称关键字
     * @param current 页码
     * @return 地点列表
     */
    @GetMapping("/of/name")
    @PublicEndpoint
    public Result queryPlaceByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Place> page = placeService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }
}
