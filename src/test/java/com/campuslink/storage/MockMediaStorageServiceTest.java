package com.campuslink.storage;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class MockMediaStorageServiceTest {

    @Test
    void avatarUploadShouldUseUserScopedObjectKey() throws Exception {
        MockMediaStorageService service = service();

        UploadResult result = service.upload(new ByteArrayInputStream(new byte[]{1}), "image/jpeg", 1, MediaScene.AVATAR, 123L, "avatar.jpg");

        assertThat(result.getObjectKey()).startsWith("avatars/123/").endsWith(".jpg");
        assertThat(result.getUrl()).startsWith("https://mock.campuslink.local/avatars/123/");
        assertThat(service.supportsRealStorage()).isFalse();
    }

    @Test
    void postUploadShouldUseUserAndMonthObjectKey() throws Exception {
        MockMediaStorageService service = service();
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        UploadResult result = service.upload(new ByteArrayInputStream(new byte[]{1}), "image/png", 1, MediaScene.POST, 123L, "post.png");

        assertThat(result.getObjectKey()).startsWith("posts/123/" + month + "/").endsWith(".png");
        assertThat(result.getUrl()).startsWith("https://mock.campuslink.local/posts/123/" + month + "/");
    }

    private MockMediaStorageService service() {
        MockMediaStorageService service = new MockMediaStorageService();
        ReflectionTestUtils.setField(service, "validator", new MediaFileValidator());
        ReflectionTestUtils.setField(service, "objectKeyFactory", new MediaObjectKeyFactory());
        return service;
    }
}
