package com.nexushub.storage;

import com.nexushub.common.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MediaFileValidatorTest {
    private final MediaFileValidator validator = new MediaFileValidator();

    @Test
    void shouldAcceptJpegPngAndWebp() {
        assertThat(validator.validate("image/jpeg", 1, MediaScene.POST, "a.jpg").getExtension()).isEqualTo("jpg");
        assertThat(validator.validate("image/png", 1, MediaScene.POST, "a.png").getExtension()).isEqualTo("png");
        assertThat(validator.validate("image/webp", 1, MediaScene.POST, "a.webp").getExtension()).isEqualTo("webp");
    }

    @Test
    void shouldRejectGifAndExe() {
        assertThatThrownBy(() -> validator.validate("image/gif", 1, MediaScene.POST, "a.gif"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> validator.validate("application/octet-stream", 1, MediaScene.POST, "a.exe"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldRejectOversizedFiles() {
        assertThatThrownBy(() -> validator.validate("image/jpeg", MediaFileValidator.AVATAR_MAX_SIZE + 1, MediaScene.AVATAR, "a.jpg"))
                .hasMessage("头像图片不能超过 5MB");
        assertThatThrownBy(() -> validator.validate("image/jpeg", MediaFileValidator.POST_MAX_SIZE + 1, MediaScene.POST, "a.jpg"))
                .hasMessage("帖子图片不能超过 10MB");
    }
}
