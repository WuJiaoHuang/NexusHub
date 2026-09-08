package com.campuslink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusPlaceSearchResponse {
    private List<CampusPlaceDTO> records;
    private long total;
    private int current;
    private int size;
}
