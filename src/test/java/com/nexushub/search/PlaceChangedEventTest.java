package com.nexushub.search;

import com.nexushub.listener.PlaceSearchSyncListener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

class PlaceChangedEventTest {

    @Test
    void listenerShouldSyncUpdatedPlace() throws Exception {
        PlaceSearchService searchService = Mockito.mock(PlaceSearchService.class);
        PlaceSearchSyncListener listener = new PlaceSearchSyncListener();
        ReflectionTestUtils.setField(listener, "placeSearchService", searchService);

        listener.onPlaceChanged(new PlaceChangedEvent(1L, PlaceChangeType.UPDATED));

        verify(searchService).saveOrUpdate(1L);
    }

    @Test
    void listenerShouldDeleteRemovedPlace() throws Exception {
        PlaceSearchService searchService = Mockito.mock(PlaceSearchService.class);
        PlaceSearchSyncListener listener = new PlaceSearchSyncListener();
        ReflectionTestUtils.setField(listener, "placeSearchService", searchService);

        listener.onPlaceChanged(new PlaceChangedEvent(2L, PlaceChangeType.DELETED));

        verify(searchService).delete(2L);
    }
}
