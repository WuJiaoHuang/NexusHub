package com.campuslink.service;

import cn.hutool.core.util.StrUtil;
import com.campuslink.common.BusinessException;
import com.campuslink.config.OssProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class AvatarUrlValidator {
    private static final String MOCK_BASE_URL = "https://mock.campuslink.local";

    @Resource
    private OssProperties ossProperties;

    public void validateForUser(String url, Long userId) {
        if (StrUtil.isBlank(url) || userId == null) {
            throw new BusinessException("头像地址不合法");
        }
        String ossPrefix = trimSlash(ossProperties.getPublicBaseUrl()) + "/avatars/" + userId + "/";
        String mockPrefix = MOCK_BASE_URL + "/avatars/" + userId + "/";
        if (!url.startsWith(ossPrefix) && !url.startsWith(mockPrefix)) {
            throw new BusinessException("头像地址不合法");
        }
    }

    private String trimSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
