package com.campuslink.controller;

import com.campuslink.dto.Result;
import com.campuslink.dto.UserDTO;
import com.campuslink.interceptor.LoginInterceptor;
import com.campuslink.storage.MediaScene;
import com.campuslink.storage.MediaStorageService;
import com.campuslink.storage.UploadResponse;
import com.campuslink.storage.UploadResult;
import com.campuslink.utils.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MediaUploadControllerTest {

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void uploadAvatarShouldReturnUploadResponse() throws Exception {
        MediaStorageService storage = mock(MediaStorageService.class);
        when(storage.upload(any(InputStream.class), eq("image/jpeg"), eq(1L), eq(MediaScene.AVATAR), eq(123L), eq("avatar.jpg")))
                .thenReturn(new UploadResult("avatars/123/a.jpg", "https://mock.campuslink.local/avatars/123/a.jpg", 1, "image/jpeg"));
        MediaUploadController controller = new MediaUploadController();
        ReflectionTestUtils.setField(controller, "mediaStorageService", storage);
        UserDTO user = new UserDTO();
        user.setId(123L);
        UserHolder.saveUser(user);

        Result result = controller.uploadImage(new MockMultipartFile("file", "avatar.jpg", "image/jpeg", new byte[]{1}), MediaScene.AVATAR);

        assertThat(result.getSuccess()).isTrue();
        UploadResponse response = (UploadResponse) result.getData();
        assertThat(response.getObjectKey()).isEqualTo("avatars/123/a.jpg");
    }

    @Test
    void loginInterceptorShouldRejectAnonymousUpload() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = new LoginInterceptor().preHandle(new MockHttpServletRequest("POST", "/uploads/image"), response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void normalUserShouldNotUploadEventImage() {
        MediaUploadController controller = new MediaUploadController();
        ReflectionTestUtils.setField(controller, "mediaStorageService", mock(MediaStorageService.class));
        UserDTO user = new UserDTO();
        user.setId(123L);
        UserHolder.saveUser(user);

        Result result = controller.uploadImage(new MockMultipartFile("file", "event.jpg", "image/jpeg", new byte[]{1}), MediaScene.EVENT);

        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getErrorMsg()).isEqualTo("当前用户无权上传该类型图片");
    }
}
