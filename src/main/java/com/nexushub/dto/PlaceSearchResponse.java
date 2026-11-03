package com.nexushub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchResponse {
    private List<PlaceDTO> records;
    private long total;
    private int current;
    private int size;
}
