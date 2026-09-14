package com.campuslink.storage;

import cn.hutool.core.util.StrUtil;
import com.campuslink.common.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MediaFileValidator {
    public static final long AVATAR_MAX_SIZE = 5L * 1024 * 1024;
    public static final long POST_MAX_SIZE = 10L * 1024 * 1024;
    public static final int MAX_POST_IMAGES = 9;

    public ValidatedMedia validate(String contentType, long size, MediaScene scene, String originalFilename) {
        if (size <= 0) {
            throw new BusinessException("请选择要上传的图片");
        }
        if (scene == MediaScene.AVATAR && size > AVATAR_MAX_SIZE) {
            throw new BusinessException("头像图片不能超过 5MB");
        }
        if (scene == MediaScene.POST && size > POST_MAX_SIZE) {
            throw new BusinessException("帖子图片不能超过 10MB");
        }
        String normalizedContentType = normalizeContentType(contentType);
        String extension = extensionFor(normalizedContentType);
        String filenameExtension = filenameExtension(originalFilename);
        if (StrUtil.isBlank(filenameExtension)) {
            throw new BusinessException("图片文件名缺少扩展名");
        }
        if ("jpg".equals(filenameExtension)) {
            filenameExtension = "jpeg";
        }
        if (!extension.equals(filenameExtension)) {
            throw new BusinessException("图片类型与文件扩展名不一致");
        }
        return new ValidatedMedia(normalizedContentType, "jpeg".equals(extension) ? "jpg" : extension);
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            throw new BusinessException("不支持的图片类型");
        }
        String normalized = contentType.toLowerCase(Locale.ROOT).trim();
        if ("image/jpg".equals(normalized)) {
            normalized = "image/jpeg";
        }
        if (!"image/jpeg".equals(normalized) && !"image/png".equals(normalized) && !"image/webp".equals(normalized)) {
            throw new BusinessException("不支持的图片类型");
        }
        return normalized;
    }

    private String extensionFor(String contentType) {
        if ("image/jpeg".equals(contentType)) {
            return "jpeg";
        }
        if ("image/png".equals(contentType)) {
            return "png";
        }
        if ("image/webp".equals(contentType)) {
            return "webp";
        }
        throw new BusinessException("不支持的图片类型");
    }

    private String filenameExtension(String originalFilename) {
        if (StrUtil.isBlank(originalFilename) || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT).trim();
    }

    public static class ValidatedMedia {
        private final String contentType;
        private final String extension;

        public ValidatedMedia(String contentType, String extension) {
            this.contentType = contentType;
            this.extension = extension;
        }

        public String getContentType() {
            return contentType;
        }

        public String getExtension() {
            return extension;
        }
    }
}
