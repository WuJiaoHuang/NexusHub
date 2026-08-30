package com.cityeats.search;

import com.cityeats.dto.ShopSearchRequest;
import com.cityeats.dto.ShopSearchResponse;

import java.io.IOException;

public interface ShopSearchService {
    ShopSearchResponse search(ShopSearchRequest request) throws IOException;
    long rebuild() throws IOException;
    void saveOrUpdate(Long shopId) throws IOException;
    void delete(Long shopId) throws IOException;
    long countDocuments() throws IOException;
}
