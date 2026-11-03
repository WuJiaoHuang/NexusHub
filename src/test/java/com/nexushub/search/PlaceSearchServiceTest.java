package com.nexushub.search;

import com.nexushub.dto.PlaceSearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceSearchServiceTest {

    @Test
    void buildQueryShouldIncludeKeywordAndFilters() {
        PlaceSearchServiceImpl service = new PlaceSearchServiceImpl();
        PlaceSearchRequest request = new PlaceSearchRequest();
        request.setKeyword("烧烤");
        request.setCategoryId(1L);
        request.setArea("浦东");
        request.setMinPrice(30L);
        request.setMaxPrice(120L);
        request.setMinScore(40);

        BoolQueryBuilder query = ReflectionTestUtils.invokeMethod(service, "buildQuery", request);

        assertThat(query).isNotNull();
        String queryJson = query.toString();
        assertThat(queryJson).contains("multi_match");
        assertThat(queryJson).contains("\"categoryId\"");
        assertThat(queryJson).contains("\"area.keyword\"");
        assertThat(queryJson).contains("\"avgPrice\"");
        assertThat(queryJson).contains("\"score\"");
    }
}
