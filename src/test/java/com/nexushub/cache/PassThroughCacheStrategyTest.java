package com.nexushub.cache;

import com.nexushub.entity.Place;
import com.nexushub.utils.CacheClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PassThroughCacheStrategyTest {

    @Test
    void strategyShouldDelegateToCacheClient() {
        CacheClient cacheClient = Mockito.mock(CacheClient.class);
        Place place = new Place().setId(1L).setName("缓存地点");
        when(cacheClient.queryWithPassThrough(eq("cache:place:"), eq(1L), eq(Place.class), any(), eq(30L), eq(TimeUnit.MINUTES)))
                .thenReturn(place);
        PassThroughCacheStrategy strategy = new PassThroughCacheStrategy();
        ReflectionTestUtils.setField(strategy, "cacheClient", cacheClient);

        Place result = strategy.query("cache:place:", 1L, Place.class, id -> null, 30L, TimeUnit.MINUTES);

        assertThat(result).isSameAs(place);
        verify(cacheClient).queryWithPassThrough(eq("cache:place:"), eq(1L), eq(Place.class), any(), eq(30L), eq(TimeUnit.MINUTES));
    }
}
