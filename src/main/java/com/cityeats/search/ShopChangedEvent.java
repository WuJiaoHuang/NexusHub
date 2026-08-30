package com.cityeats.search;

import lombok.Getter;

@Getter
public class ShopChangedEvent {
    private final Long shopId;
    private final ShopChangeType type;

    public ShopChangedEvent(Long shopId, ShopChangeType type) {
        this.shopId = shopId;
        this.type = type;
    }
}
