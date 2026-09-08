package com.campuslink.service;

import com.campuslink.dto.Result;

public interface EventRecommendationService {
    Result recommend(Integer limit);
}
