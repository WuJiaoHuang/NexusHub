package com.campuslink.search;

import lombok.Getter;

@Getter
public class CampusPlaceChangedEvent {
    private final Long placeId;
    private final CampusPlaceChangeType type;

    public CampusPlaceChangedEvent(Long placeId, CampusPlaceChangeType type) {
        this.placeId = placeId;
        this.type = type;
    }
}
