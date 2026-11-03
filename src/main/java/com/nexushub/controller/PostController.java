package com.nexushub.controller;

import com.nexushub.security.PublicEndpoint;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexushub.dto.Result;
import com.nexushub.dto.UserDTO;
import com.nexushub.entity.Post;
import com.nexushub.entity.User;
import com.nexushub.service.IPostService;
import com.nexushub.service.IUserService;
import com.nexushub.utils.SystemConstants;
import com.nexushub.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/posts")
public class PostController {

    @Resource
    private IPostService postService;
    @Resource
    private IUserService userService;

    @PostMapping
    public Result savePost(@RequestBody Post post) {
       return postService.savePost(post);
    }

    @PutMapping("/like/{id}")
    public Result likePost(@PathVariable("id") Long id) {
        // 修改点赞数量
        return postService.updateLike(id);
    }

    @GetMapping("/of/me")
    public Result queryMyPost(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Post> page = postService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Post> records = page.getRecords();
        return Result.ok(records);
    }

    /**
     * 分页查询
     * @param current
     * @return
     */
    @GetMapping("/hot")
    @PublicEndpoint
    public Result queryHotPost(@RequestParam(value = "current", defaultValue = "1") Integer current) {
       return postService.queryHotPost(current);
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result queryById(@PathVariable("id") Long id){
        return postService.queryPostById(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryPostLikes(@PathVariable("id") Long id){
        return postService.queryPostLikes(id);
    }

    @GetMapping("/of/user")
    public Result queryPostByUserId(
            @RequestParam(value = "current",defaultValue = "1") Integer currrent,
            @RequestParam("id") Long id){
        //根据用户查询
        Page<Post> page = postService.query()
                .eq("user_id", id).page(new Page<>(currrent, SystemConstants.MAX_PAGE_SIZE));

        //获取当前页数据
        List<Post> records = page.getRecords();
        return Result.ok(records);
    }
    @GetMapping("/feeds")
    public Result queryPostOfFollow(
            @RequestParam("lastId") Long max,
            @RequestParam(value = "offset",defaultValue = "0") Integer offset){
        return postService.queryPostOfFollow(max,offset);

    }
}


