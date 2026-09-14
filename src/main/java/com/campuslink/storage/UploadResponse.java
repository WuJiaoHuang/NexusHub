package com.campuslink.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private String objectKey;
    private String url;
    private Long size;
    private String contentType;

    public static UploadResponse from(UploadResult result) {
        return new UploadResponse(result.getObjectKey(), result.getUrl(), result.getSize(), result.getContentType());
    }
}
