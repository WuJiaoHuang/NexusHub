package com.nexushub.search;

import com.nexushub.dto.PlaceSearchRequest;
import com.nexushub.dto.PlaceSearchResponse;

import java.io.IOException;

public interface PlaceSearchService {
    PlaceSearchResponse search(PlaceSearchRequest request) throws IOException;
    long rebuild() throws IOException;
    void saveOrUpdate(Long placeId) throws IOException;
    void delete(Long placeId) throws IOException;
    long countDocuments() throws IOException;
}
