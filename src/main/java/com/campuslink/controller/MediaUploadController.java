package com.campuslink.controller;

import com.campuslink.common.BusinessException;
import com.campuslink.dto.Result;
import com.campuslink.dto.UserDTO;
import com.campuslink.storage.MediaScene;
import com.campuslink.storage.MediaStorageService;
import com.campuslink.storage.UploadResponse;
import com.campuslink.storage.UploadResult;
import com.campuslink.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/uploads")
public class MediaUploadController {

    @Resource
    private MediaStorageService mediaStorageService;

    @PostMapping("/image")
    public Result uploadImage(@RequestParam("file") MultipartFile file,
                              @RequestParam("scene") MediaScene scene) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        if (scene != MediaScene.AVATAR && scene != MediaScene.POST) {
            return Result.fail("当前用户无权上传该类型图片");
        }
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要上传的图片");
        }
        try {
            UploadResult result = mediaStorageService.upload(
                    file.getInputStream(),
                    file.getContentType(),
                    file.getSize(),
                    scene,
                    user.getId(),
                    file.getOriginalFilename());
            return Result.ok(UploadResponse.from(result));
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (IOException e) {
            log.error("OSS upload failed, scene={}, userId={}", scene, user.getId(), e);
            return Result.fail("上传失败，请检查网络后重试");
        } catch (RuntimeException e) {
            log.error("OSS upload failed, scene={}, userId={}", scene, user.getId(), e);
            return Result.fail("上传失败，请检查网络后重试");
        }
    }
}
