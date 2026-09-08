package com.campuslink.cache;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CacheStrategyFactoryTest {

    @Test
    void factoryShouldReturnStrategyByType() {
        CacheQueryStrategy passThrough = Mockito.mock(CacheQueryStrategy.class);
        CacheQueryStrategy mutex = Mockito.mock(CacheQueryStrategy.class);
        Mockito.when(passThrough.type()).thenReturn(CacheStrategyType.PASS_THROUGH);
        Mockito.when(mutex.type()).thenReturn(CacheStrategyType.MUTEX);

        CacheStrategyFactory factory = new CacheStrategyFactory(Arrays.asList(passThrough, mutex));

        assertThat(factory.get(CacheStrategyType.PASS_THROUGH)).isSameAs(passThrough);
        assertThat(factory.get(CacheStrategyType.MUTEX)).isSameAs(mutex);
    }
}
