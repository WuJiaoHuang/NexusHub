package com.cityeats.dto;

import lombok.Data;

@Data
public class ShopSearchRequest {
    private String keyword;
    private Long typeId;
    private String area;
    private Long minPrice;
    private Long maxPrice;
    private Integer minScore;
    private Integer current = 1;
    private Integer size = 10;
    private String sort;

    public int safeCurrent() {
        return current == null || current < 1 ? 1 : current;
    }

    public int safeSize() {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
