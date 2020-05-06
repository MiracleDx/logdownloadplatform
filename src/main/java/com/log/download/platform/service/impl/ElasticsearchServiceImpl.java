package com.log.download.platform.service.impl;

import com.log.download.platform.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-04-30 10:11
 **/
@Slf4j
@Service("esserver")
public class ElasticsearchServiceImpl implements ElasticsearchService {

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
    @Override
    public SearchHits getIndexDocument(String indices,
                                       String[] includes,
                                       String[] excludes,
                                       String orderName,
                                       Boolean orderRule,
                                       BoolQueryBuilder boolBuilder,
                                       RestHighLevelClient client) throws IOException {
        return getIndexDocumentLimit(indices, null, null,
                includes, excludes, orderName, orderRule, boolBuilder, client);
    }

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
    @Override
    public SearchHits getIndexDocumentMatchAll(String indices,
                                               String[] includes,
                                               String[] excludes,
                                               String orderName,
                                               Boolean orderRule,
                                               RestHighLevelClient client) throws IOException {
        return getIndexDocumentLimit(indices, null, null, includes,
                excludes, orderName, orderRule, null, client);
    }

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
    @Override
    public SearchHits getIndexDocumentLimit(String indices,
                                            Integer from,
                                            Integer size,
                                            String[] includes,
                                            String[] excludes,
                                            String orderName,
                                            Boolean orderRule,
                                            BoolQueryBuilder boolBuilder,
                                            RestHighLevelClient client) throws IOException {
        //1、构造sourceBuild(source源)
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(from == null || size < 0 ? 0 : from);
        searchSourceBuilder.size(size == null || size < 1 ? 1000 : size);
        searchSourceBuilder.fetchSource(includes, excludes);
        // 设置排序规则
        if (orderRule) {
            searchSourceBuilder.sort(orderName, SortOrder.ASC);
        } else {
            searchSourceBuilder.sort(orderName, SortOrder.DESC);
        }
        // 设置查询条件
        if (boolBuilder == null) {
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        } else {
            searchSourceBuilder.query(boolBuilder);
        }
        //2、构造查询请求对象
        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.source(searchSourceBuilder);
        //3、client 执行查询
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        //4、返回结果
        return searchResponse.getHits();
    }

    /**
     * 部分更新es索引内容
     *
     * @param indexName
     * @param indexID
     * @param jsonMap   更新的内容
     * @param client
     */
    @Override
    public void updateTage(String indexName, String indexID, Map<String, Object> jsonMap, RestHighLevelClient client) {
        UpdateRequest updateRequest = new UpdateRequest(indexName, indexID).doc(jsonMap);
        try {
            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
