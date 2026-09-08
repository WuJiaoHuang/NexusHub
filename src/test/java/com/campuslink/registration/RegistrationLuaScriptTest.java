package com.campuslink.registration;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationLuaScriptTest {

    @Test
    void scriptShouldContainAtomicStockAndDuplicateChecks() throws Exception {
        String script = StreamUtils.copyToString(new ClassPathResource("lua/event_registration.lua").getInputStream(), StandardCharsets.UTF_8);
        assertThat(script).contains("event:slots:");
        assertThat(script).contains("event:registration:");
        assertThat(script).contains("sismember");
        assertThat(script).contains("incrby");
        assertThat(script).contains("sadd");
    }
}
