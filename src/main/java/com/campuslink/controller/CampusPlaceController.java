package com.campuslink.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.dto.Result;
import com.campuslink.dto.CampusPlaceSearchRequest;
import com.campuslink.entity.CampusPlace;
import com.campuslink.service.ICampusPlaceService;
import com.campuslink.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/places")
public class CampusPlaceController {

    @Resource
    public ICampusPlaceService campusPlaceService;

    /**
     * 根据id查询校园地点信息
     *
     * @param id 校园地点id
     * @return 校园地点详情数据
     */
    @GetMapping("/{id}")
    public Result queryCampusPlaceById(@PathVariable("id") Long id) throws InterruptedException {
        return campusPlaceService.queryById(id);
    }

    /**
     * 新增校园地点信息
     * @param place 校园地点数据
     * @return 校园地点id
     */
    @PostMapping
    public Result saveCampusPlace(@RequestBody CampusPlace campusPlace) {
        // 写入数据库
        campusPlaceService.save(campusPlace);
        // 返回校园地点id
        return Result.ok(campusPlace.getId());
    }

    /**
     * 更新校园地点信息
     * @param place 校园地点数据
     * @return 无
     */
    @PutMapping
    public Result updateCampusPlace(@RequestBody CampusPlace campusPlace) {
        // 写入数据库

        return campusPlaceService.update(campusPlace);
    }

    /**
     * 根据校园地点类型分页查询校园地点信息
     * @param categoryId 校园地点类型
     * @param current 页码
     * @return 校园地点列表
     */
    @GetMapping("/nearby")
    public Result queryCampusPlaceByType(
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "x",required = false) Double x,
            @RequestParam(value = "y",required = false) Double y
    ) {
        return campusPlaceService.queryCampusPlaceByType(categoryId,current,x,y);
    }

    @GetMapping("/search")
    public Result search(CampusPlaceSearchRequest request) {
        return campusPlaceService.search(request);
    }

    /**
     * 根据校园地点名称关键字分页查询校园地点信息
     * @param name 校园地点名称关键字
     * @param current 页码
     * @return 校园地点列表
     */
    @GetMapping("/of/name")
    public Result queryCampusPlaceByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<CampusPlace> page = campusPlaceService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }
}


