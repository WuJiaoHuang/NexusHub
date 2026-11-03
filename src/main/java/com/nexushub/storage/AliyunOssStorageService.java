package com.nexushub.storage;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.nexushub.config.OssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "aliyun.oss", name = "enabled", havingValue = "true")
public class AliyunOssStorageService implements MediaStorageService {
    @Resource
    private OssProperties properties;
    @Resource
    private MediaFileValidator validator;
    @Resource
    private MediaObjectKeyFactory objectKeyFactory;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        if (StrUtil.isBlank(properties.getAccessKeyId()) || StrUtil.isBlank(properties.getAccessKeySecret())) {
            throw new IllegalStateException("Aliyun OSS is enabled but access key configuration is missing");
        }
        ossClient = new OSSClientBuilder().build(properties.getEndpoint(), properties.getAccessKeyId(), properties.getAccessKeySecret());
    }

    @Override
    public UploadResult upload(InputStream inputStream, String contentType, long contentLength, MediaScene scene, Long userId, String originalFilename) throws IOException {
        MediaFileValidator.ValidatedMedia media = validator.validate(contentType, contentLength, scene, originalFilename);
        String objectKey = objectKeyFactory.create(scene, userId, media.getExtension());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(media.getContentType());
        metadata.setContentLength(contentLength);
        ossClient.putObject(properties.getBucketName(), objectKey, inputStream, metadata);
        String baseUrl = properties.getPublicBaseUrl().endsWith("/")
                ? properties.getPublicBaseUrl().substring(0, properties.getPublicBaseUrl().length() - 1)
                : properties.getPublicBaseUrl();
        return new UploadResult(objectKey, baseUrl + "/" + objectKey, contentLength, media.getContentType());
    }

    @Override
    public void delete(String objectKey) {
        if (StrUtil.isNotBlank(objectKey)) {
            ossClient.deleteObject(properties.getBucketName(), objectKey);
        }
    }

    @Override
    public boolean supportsRealStorage() {
        return true;
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
