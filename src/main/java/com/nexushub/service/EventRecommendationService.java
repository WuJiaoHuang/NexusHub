package com.nexushub.service;

import com.nexushub.dto.Result;

public interface EventRecommendationService {
    Result recommend(Integer limit);
}
