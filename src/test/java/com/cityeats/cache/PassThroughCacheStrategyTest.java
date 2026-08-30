package com.cityeats.cache;

import com.cityeats.entity.Shop;
import com.cityeats.utils.CacheClient;
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
        Shop shop = new Shop().setId(1L).setName("缓存商户");
        when(cacheClient.queryWithPassThrough(eq("cache:shop:"), eq(1L), eq(Shop.class), any(), eq(30L), eq(TimeUnit.MINUTES)))
                .thenReturn(shop);
        PassThroughCacheStrategy strategy = new PassThroughCacheStrategy();
        ReflectionTestUtils.setField(strategy, "cacheClient", cacheClient);

        Shop result = strategy.query("cache:shop:", 1L, Shop.class, id -> null, 30L, TimeUnit.MINUTES);

        assertThat(result).isSameAs(shop);
        verify(cacheClient).queryWithPassThrough(eq("cache:shop:"), eq(1L), eq(Shop.class), any(), eq(30L), eq(TimeUnit.MINUTES));
    }
}
