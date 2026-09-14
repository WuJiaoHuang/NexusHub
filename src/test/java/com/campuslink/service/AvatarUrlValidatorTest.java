package com.campuslink.service;

import com.campuslink.common.BusinessException;
import com.campuslink.config.OssProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarUrlValidatorTest {

    @Test
    void shouldAllowCurrentUserOssOrMockAvatarUrlOnly() {
        AvatarUrlValidator validator = new AvatarUrlValidator();
        OssProperties properties = new OssProperties();
        properties.setPublicBaseUrl("https://java-ai-wujiaohuang.oss-cn-beijing.aliyuncs.com");
        ReflectionTestUtils.setField(validator, "ossProperties", properties);

        assertThatCode(() -> validator.validateForUser("https://java-ai-wujiaohuang.oss-cn-beijing.aliyuncs.com/avatars/123/a.jpg", 123L))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validateForUser("https://mock.campuslink.local/avatars/123/a.jpg", 123L))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> validator.validateForUser("https://java-ai-wujiaohuang.oss-cn-beijing.aliyuncs.com/avatars/456/a.jpg", 123L))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> validator.validateForUser("https://evil.com/x.jpg", 123L))
                .isInstanceOf(BusinessException.class);
    }
}
