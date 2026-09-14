package com.campuslink.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {
    private String objectKey;
    private String url;
    private long size;
    private String contentType;
}
