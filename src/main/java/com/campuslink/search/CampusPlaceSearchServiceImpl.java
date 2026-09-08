package com.campuslink.search;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.dto.CampusPlaceDTO;
import com.campuslink.dto.CampusPlaceSearchRequest;
import com.campuslink.dto.CampusPlaceSearchResponse;
import com.campuslink.entity.CampusPlace;
import com.campuslink.service.ICampusPlaceService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CampusPlaceSearchServiceImpl implements CampusPlaceSearchService {

    private static final String INDEX_MAPPING = "{"
            + "\"mappings\":{\"properties\":{"
            + "\"id\":{\"type\":\"long\"},"
            + "\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},"
            + "\"categoryId\":{\"type\":\"long\"},"
            + "\"images\":{\"type\":\"keyword\",\"index\":false},"
            + "\"area\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},"
            + "\"address\":{\"type\":\"text\"},"
            + "\"description\":{\"type\":\"text\"},"
            + "\"avgPrice\":{\"type\":\"long\"},"
            + "\"score\":{\"type\":\"integer\"},"
            + "\"sold\":{\"type\":\"integer\"},"
            + "\"comments\":{\"type\":\"integer\"},"
            + "\"x\":{\"type\":\"double\"},"
            + "\"y\":{\"type\":\"double\"},"
            + "\"openHours\":{\"type\":\"keyword\"},"
            + "\"createTime\":{\"type\":\"date\",\"format\":\"strict_date_optional_time||yyyy-MM-dd HH:mm:ss\"},"
            + "\"updateTime\":{\"type\":\"date\",\"format\":\"strict_date_optional_time||yyyy-MM-dd HH:mm:ss\"}"
            + "}}}";

    @Resource
    private RestHighLevelClient client;
    @Lazy
    @Resource
    private ICampusPlaceService placeService;
    @Resource
    private CampusPlaceSearchConverter converter;

    @Value("${campuslink.search.place-index:campuslink_place}")
    private String indexName;

    @Value("${campuslink.search.rebuild-batch-size:500}")
    private int rebuildBatchSize;

    @Override
    public CampusPlaceSearchResponse search(CampusPlaceSearchRequest request) throws IOException {
        ensureIndex();
        CampusPlaceSearchRequest safeRequest = request == null ? new CampusPlaceSearchRequest() : request;
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(buildQuery(safeRequest))
                .from((safeRequest.safeCurrent() - 1) * safeRequest.safeSize())
                .size(safeRequest.safeSize());
        applySort(sourceBuilder, safeRequest.getSort());

        SearchResponse response = client.search(new SearchRequest(indexName).source(sourceBuilder), RequestOptions.DEFAULT);
        List<CampusPlaceDTO> records = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            CampusPlaceDocument document = JSONUtil.toBean(hit.getSourceAsString(), CampusPlaceDocument.class);
            records.add(converter.toDto(document));
        }
        long total = response.getHits().getTotalHits() == null ? records.size() : response.getHits().getTotalHits().value;
        return new CampusPlaceSearchResponse(records, total, safeRequest.safeCurrent(), safeRequest.safeSize());
    }

    @Override
    public long rebuild() throws IOException {
        recreateIndex();
        long total = placeService.count();
        long synced = 0;
        long current = 1;
        int batchSize = Math.max(rebuildBatchSize, 1);
        while (synced < total) {
            Page<CampusPlace> page = placeService.page(new Page<>(current, batchSize));
            List<CampusPlaceDocument> documents = converter.toDocuments(page.getRecords());
            if (documents.isEmpty()) {
                break;
            }
            bulkSave(documents);
            synced += documents.size();
            current++;
        }
        log.info("校园地点搜索索引全量重建完成，index={}, count={}", indexName, synced);
        return synced;
    }

    @Override
    public void saveOrUpdate(Long placeId) throws IOException {
        if (placeId == null) {
            return;
        }
        ensureIndex();
        CampusPlace place = placeService.getById(placeId);
        if (place == null) {
            delete(placeId);
            return;
        }
        CampusPlaceDocument document = converter.toDocument(place);
        IndexRequest request = new IndexRequest(indexName)
                .id(String.valueOf(document.getId()))
                .source(JSONUtil.toJsonStr(document), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
        log.info("校园地点搜索索引增量同步完成，placeId={}", placeId);
    }

    @Override
    public void delete(Long placeId) throws IOException {
        if (placeId == null) {
            return;
        }
        ensureIndex();
        client.delete(new DeleteRequest(indexName, String.valueOf(placeId)), RequestOptions.DEFAULT);
        log.info("校园地点搜索索引删除完成，placeId={}", placeId);
    }

    @Override
    public long countDocuments() throws IOException {
        ensureIndex();
        CountResponse response = client.count(new CountRequest(indexName), RequestOptions.DEFAULT);
        return response.getCount();
    }

    private BoolQueryBuilder buildQuery(CampusPlaceSearchRequest request) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StrUtil.isNotBlank(request.getKeyword())) {
            boolQuery.must(QueryBuilders.multiMatchQuery(request.getKeyword(), "name", "description", "address", "area")
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS));
        } else {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        if (request.getCategoryId() != null) {
            boolQuery.filter(QueryBuilders.termQuery("categoryId", request.getCategoryId()));
        }
        if (StrUtil.isNotBlank(request.getArea())) {
            boolQuery.filter(QueryBuilders.termQuery("area.keyword", request.getArea()));
        }
        RangeQueryBuilder priceRange = QueryBuilders.rangeQuery("avgPrice");
        boolean hasPriceRange = false;
        if (request.getMinPrice() != null) {
            priceRange.gte(request.getMinPrice());
            hasPriceRange = true;
        }
        if (request.getMaxPrice() != null) {
            priceRange.lte(request.getMaxPrice());
            hasPriceRange = true;
        }
        if (hasPriceRange) {
            boolQuery.filter(priceRange);
        }
        if (request.getMinScore() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("score").gte(request.getMinScore()));
        }
        return boolQuery;
    }

    private void applySort(SearchSourceBuilder sourceBuilder, String sort) {
        if ("score_desc".equalsIgnoreCase(sort)) {
            sourceBuilder.sort("score", SortOrder.DESC);
        } else if ("sold_desc".equalsIgnoreCase(sort)) {
            sourceBuilder.sort("sold", SortOrder.DESC);
        } else if ("avgPrice_asc".equalsIgnoreCase(sort)) {
            sourceBuilder.sort("avgPrice", SortOrder.ASC);
        } else if ("avgPrice_desc".equalsIgnoreCase(sort)) {
            sourceBuilder.sort("avgPrice", SortOrder.DESC);
        }
    }

    private void recreateIndex() throws IOException {
        if (indexExists()) {
            client.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
        }
        client.indices().create(new CreateIndexRequest(indexName).source(INDEX_MAPPING, XContentType.JSON), RequestOptions.DEFAULT);
    }

    private void ensureIndex() throws IOException {
        if (!indexExists()) {
            client.indices().create(new CreateIndexRequest(indexName).source(INDEX_MAPPING, XContentType.JSON), RequestOptions.DEFAULT);
        }
    }

    private boolean indexExists() throws IOException {
        return client.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
    }

    private void bulkSave(List<CampusPlaceDocument> documents) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (CampusPlaceDocument document : documents) {
            bulkRequest.add(new IndexRequest(indexName)
                    .id(String.valueOf(document.getId()))
                    .source(JSONUtil.toJsonStr(document), XContentType.JSON));
        }
        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (response.hasFailures()) {
            throw new IOException("校园地点搜索索引批量写入失败: " + response.buildFailureMessage());
        }
    }
}
