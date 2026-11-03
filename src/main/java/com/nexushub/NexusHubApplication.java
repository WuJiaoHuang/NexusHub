package com.nexushub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.nexushub.mapper")
@SpringBootApplication
public class NexusHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusHubApplication.class, args);
    }
}
