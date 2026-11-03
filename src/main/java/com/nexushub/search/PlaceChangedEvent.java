package com.nexushub.search;

import lombok.Getter;

@Getter
public class PlaceChangedEvent {
    private final Long placeId;
    private final PlaceChangeType type;

    public PlaceChangedEvent(Long placeId, PlaceChangeType type) {
        this.placeId = placeId;
        this.type = type;
    }
}
