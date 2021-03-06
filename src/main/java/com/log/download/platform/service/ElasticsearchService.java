package com.log.download.platform.service;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public interface ElasticsearchService {

    /**
     * 带查询条件，不带分页，表示为全查询
     *
     * @param indices     索引名
     * @param includes    包含字段
     * @param excludes    不包含字段
     * @param orderName   排序字段
     * @param orderRule   排序规则true-asc false-desc
     * @param boolBuilder 查询规则
     * @param client      客户端
     * @return SearchHits
     */
    SearchHits getIndexDocument(String indices, String[] includes,
                                String[] excludes, String orderName,
                                Boolean orderRule, BoolQueryBuilder boolBuilder,
                                RestHighLevelClient client) throws IOException;

    /**
     * 不带查询条件的查询
     *
     * @param indices   索引名
     * @param includes  包含字段
     * @param excludes  不包含字段
     * @param orderName 排序字段
     * @param orderRule 排序规则true-asc false-desc
     * @param client    客户端
     * @return SearchHits
     */
    SearchHits getIndexDocumentMatchAll(String indices, String[] includes,
                                        String[] excludes, String orderName,
                                        Boolean orderRule, RestHighLevelClient client) throws IOException;

    /**
     * 带查询条件，带分页，可以表示查询里的limit
     *
     * @param indices     索引名
     * @param from        分页起始
     * @param size        分页大小
     * @param includes    包含字段
     * @param excludes    不包含字段
     * @param orderName   排序字段
     * @param orderRule   排序规则true-asc false-desc
     * @param boolBuilder 查询规则
     * @param client      客户端
     * @return SearchHits
     */
    SearchHits getIndexDocumentLimit(String indices, Integer from, Integer size, String[] includes,
                                     String[] excludes, String orderName,
                                     Boolean orderRule, BoolQueryBuilder boolBuilder,
                                     RestHighLevelClient client) throws IOException;

    /**
     * 部分更新es索引内容
     *
     * @param indexName
     * @param indexID
     * @param jsonMap   更新的内容
     * @param client
     */
    void updateTage (String indexName, String indexID, Map<String, Object> jsonMap, RestHighLevelClient client);

    boolean isExists(String indexName, String indexID, RestHighLevelClient client);

    void creatIndex (String indexName, String indexID, Map<String, Object> jsonMap, RestHighLevelClient client);
}
