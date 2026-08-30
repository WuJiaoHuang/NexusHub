package com.cityeats;

import com.cityeats.mapper.ShopMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class CityEatsApplicationTests {

    @Autowired
    private ShopMapper shopMapper;

    @Test
    void contextLoadsAndMapperCanQuery() {
        assertThat(shopMapper.selectById(1L)).isNotNull();
    }
}
