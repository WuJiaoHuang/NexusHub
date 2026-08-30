package com.cityeats.search;

import com.cityeats.listener.ShopSearchSyncListener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

class ShopChangedEventTest {

    @Test
    void listenerShouldSyncUpdatedShop() throws Exception {
        ShopSearchService searchService = Mockito.mock(ShopSearchService.class);
        ShopSearchSyncListener listener = new ShopSearchSyncListener();
        ReflectionTestUtils.setField(listener, "shopSearchService", searchService);

        listener.onShopChanged(new ShopChangedEvent(1L, ShopChangeType.UPDATED));

        verify(searchService).saveOrUpdate(1L);
    }

    @Test
    void listenerShouldDeleteRemovedShop() throws Exception {
        ShopSearchService searchService = Mockito.mock(ShopSearchService.class);
        ShopSearchSyncListener listener = new ShopSearchSyncListener();
        ReflectionTestUtils.setField(listener, "shopSearchService", searchService);

        listener.onShopChanged(new ShopChangedEvent(2L, ShopChangeType.DELETED));

        verify(searchService).delete(2L);
    }
}
