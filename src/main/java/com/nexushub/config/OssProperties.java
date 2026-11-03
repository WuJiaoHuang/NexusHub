package com.nexushub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {
    private boolean enabled;
    private String endpoint;
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;
    private String publicBaseUrl;
}
