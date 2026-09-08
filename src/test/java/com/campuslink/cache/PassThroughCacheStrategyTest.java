package com.campuslink.cache;

import com.campuslink.entity.CampusPlace;
import com.campuslink.utils.CacheClient;
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
        CampusPlace place = new CampusPlace().setId(1L).setName("缓存校园地点");
        when(cacheClient.queryWithPassThrough(eq("cache:place:"), eq(1L), eq(CampusPlace.class), any(), eq(30L), eq(TimeUnit.MINUTES)))
                .thenReturn(place);
        PassThroughCacheStrategy strategy = new PassThroughCacheStrategy();
        ReflectionTestUtils.setField(strategy, "cacheClient", cacheClient);

        CampusPlace result = strategy.query("cache:place:", 1L, CampusPlace.class, id -> null, 30L, TimeUnit.MINUTES);

        assertThat(result).isSameAs(place);
        verify(cacheClient).queryWithPassThrough(eq("cache:place:"), eq(1L), eq(CampusPlace.class), any(), eq(30L), eq(TimeUnit.MINUTES));
    }
}
