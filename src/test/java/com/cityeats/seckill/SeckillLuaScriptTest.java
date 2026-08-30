package com.cityeats.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SeckillLuaScriptTest {

    @Test
    void scriptShouldContainAtomicStockAndDuplicateChecks() throws Exception {
        String script = StreamUtils.copyToString(new ClassPathResource("lua/seckill.lua").getInputStream(), StandardCharsets.UTF_8);
        assertThat(script).contains("seckill:stock:");
        assertThat(script).contains("seckill:order:");
        assertThat(script).contains("sismember");
        assertThat(script).contains("incrby");
        assertThat(script).contains("sadd");
    }
}
