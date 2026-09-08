package com.campuslink.search;

import com.campuslink.dto.CampusPlaceSearchRequest;
import com.campuslink.dto.CampusPlaceSearchResponse;

import java.io.IOException;

public interface CampusPlaceSearchService {
    CampusPlaceSearchResponse search(CampusPlaceSearchRequest request) throws IOException;
    long rebuild() throws IOException;
    void saveOrUpdate(Long placeId) throws IOException;
    void delete(Long placeId) throws IOException;
    long countDocuments() throws IOException;
}
