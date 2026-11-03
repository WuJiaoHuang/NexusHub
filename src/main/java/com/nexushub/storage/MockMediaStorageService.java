package com.nexushub.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

@Service
@ConditionalOnProperty(prefix = "aliyun.oss", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockMediaStorageService implements MediaStorageService {
    private static final String MOCK_BASE_URL = "https://mock.nexushub.local";

    @Resource
    private MediaFileValidator validator;
    @Resource
    private MediaObjectKeyFactory objectKeyFactory;

    @Override
    public UploadResult upload(InputStream inputStream, String contentType, long contentLength, MediaScene scene, Long userId, String originalFilename) throws IOException {
        MediaFileValidator.ValidatedMedia media = validator.validate(contentType, contentLength, scene, originalFilename);
        String objectKey = objectKeyFactory.create(scene, userId, media.getExtension());
        return new UploadResult(objectKey, MOCK_BASE_URL + "/" + objectKey, contentLength, media.getContentType());
    }

    @Override
    public void delete(String objectKey) {
        // Mock storage keeps no remote object.
    }

    @Override
    public boolean supportsRealStorage() {
        return false;
    }
}
