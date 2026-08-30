package com.cityeats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopSearchResponse {
    private List<ShopDTO> records;
    private long total;
    private int current;
    private int size;
}
