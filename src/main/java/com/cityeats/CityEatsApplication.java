package com.cityeats;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.cityeats.mapper")
@SpringBootApplication
public class CityEatsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CityEatsApplication.class, args);
    }
}
