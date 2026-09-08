package com.campuslink.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.dto.Result;
import com.campuslink.dto.UserDTO;
import com.campuslink.entity.CampusPost;
import com.campuslink.entity.User;
import com.campuslink.service.ICampusPostService;
import com.campuslink.service.IUserService;
import com.campuslink.utils.SystemConstants;
import com.campuslink.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/posts")
public class CampusPostController {

    @Resource
    private ICampusPostService campusPostService;
    @Resource
    private IUserService userService;

    @PostMapping
    public Result saveCampusPost(@RequestBody CampusPost campusPost) {
       return campusPostService.saveCampusPost(campusPost);
    }

    @PutMapping("/like/{id}")
    public Result likeCampusPost(@PathVariable("id") Long id) {
        // 修改点赞数量
        return campusPostService.updateLike(id);
    }

    @GetMapping("/of/me")
    public Result queryMyCampusPost(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<CampusPost> page = campusPostService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<CampusPost> records = page.getRecords();
        return Result.ok(records);
    }

    /**
     * 分页查询
     * @param current
     * @return
     */
    @GetMapping("/hot")
    public Result queryHotCampusPost(@RequestParam(value = "current", defaultValue = "1") Integer current) {
       return campusPostService.queryHotCampusPost(current);
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result queryById(@PathVariable("id") Long id){
        return campusPostService.queryCampusPostById(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryCampusPostLikes(@PathVariable("id") Long id){
        return campusPostService.queryCampusPostLikes(id);
    }

    @GetMapping("/of/user")
    public Result queryCampusPostByUserId(
            @RequestParam(value = "current",defaultValue = "1") Integer currrent,
            @RequestParam("id") Long id){
        //根据用户查询
        Page<CampusPost> page = campusPostService.query()
                .eq("user_id", id).page(new Page<>(currrent, SystemConstants.MAX_PAGE_SIZE));

        //获取当前页数据
        List<CampusPost> records = page.getRecords();
        return Result.ok(records);
    }
    @GetMapping("/feeds")
    public Result queryCampusPostOfFollow(
            @RequestParam("lastId") Long max,
            @RequestParam(value = "offset",defaultValue = "0") Integer offset){
        return campusPostService.quertCampusPostOfFollow(max,offset);

    }
}

