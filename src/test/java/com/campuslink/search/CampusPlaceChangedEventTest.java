package com.campuslink.search;

import com.campuslink.listener.CampusPlaceSearchSyncListener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

class CampusPlaceChangedEventTest {

    @Test
    void listenerShouldSyncUpdatedCampusPlace() throws Exception {
        CampusPlaceSearchService searchService = Mockito.mock(CampusPlaceSearchService.class);
        CampusPlaceSearchSyncListener listener = new CampusPlaceSearchSyncListener();
        ReflectionTestUtils.setField(listener, "placeSearchService", searchService);

        listener.onCampusPlaceChanged(new CampusPlaceChangedEvent(1L, CampusPlaceChangeType.UPDATED));

        verify(searchService).saveOrUpdate(1L);
    }

    @Test
    void listenerShouldDeleteRemovedCampusPlace() throws Exception {
        CampusPlaceSearchService searchService = Mockito.mock(CampusPlaceSearchService.class);
        CampusPlaceSearchSyncListener listener = new CampusPlaceSearchSyncListener();
        ReflectionTestUtils.setField(listener, "placeSearchService", searchService);

        listener.onCampusPlaceChanged(new CampusPlaceChangedEvent(2L, CampusPlaceChangeType.DELETED));

        verify(searchService).delete(2L);
    }
}
