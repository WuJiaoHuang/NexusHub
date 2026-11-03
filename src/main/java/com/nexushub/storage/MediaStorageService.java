package com.nexushub.storage;

import java.io.IOException;
import java.io.InputStream;

public interface MediaStorageService {
    UploadResult upload(InputStream inputStream,
                        String contentType,
                        long contentLength,
                        MediaScene scene,
                        Long userId,
                        String originalFilename) throws IOException;

    void delete(String objectKey);

    boolean supportsRealStorage();
}
