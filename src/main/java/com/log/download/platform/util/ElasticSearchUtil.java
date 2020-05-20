package com.log.download.platform.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-04-30 10:16
 **/
public class ElasticSearchUtil {

    private Logger logger = LoggerFactory.getLogger(ElasticSearchUtil.class);

    private ElasticSearchUtil() {

    }

    private static class SingletonInstance {
        private static final ElasticSearchUtil INSTANCE = new ElasticSearchUtil();
    }

    public static ElasticSearchUtil getInstance() {
        return ElasticSearchUtil.SingletonInstance.INSTANCE;
    }
    
    private RestHighLevelClient client;
    
    public void init(Environment environment, String[] esUris) {
        this.client = getClient(environment, esUris);
    }

    /**
     * 初始化高级客户端
     *
     * @return 返回初始化后的es查询客户端
     */
    public RestHighLevelClient getClient(Environment environment, String[] esUris) {
        String userName = environment.getProperty("spring.elasticsearch.rest.username");
        String password = environment.getProperty("spring.elasticsearch.rest.password");
        HttpHost[] httpHosts = new HttpHost[esUris.length];
        //将地址转换为http主机数组，未配置端口则采用默认9200端口，配置了端口则用配置的端口
        for (int i = 0; i < httpHosts.length; i++) {
            if (!StringUtils.isEmpty(esUris[i])) {
                //若有密码生成带密码的高级客户端
                if (esUris[i].contains(":")) {
                    String[] uris = esUris[i].split(":");
                    httpHosts[i] = new HttpHost(uris[0], Integer.parseInt(uris[1]), "http");
                } else {
                    httpHosts[i] = new HttpHost(esUris[i], 9200, "http");
                }
            }
        }
        //如果没有用户名和密码就生成简单客户端
        if (StringUtils.isEmpty(userName)) {
            return new RestHighLevelClient(RestClient.builder(httpHosts));
        } else {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    //es账号密码
                    new UsernamePasswordCredentials(userName, password));
            return new RestHighLevelClient(
                    RestClient.builder(httpHosts)
                            .setHttpClientConfigCallback((httpClientBuilder) -> {
                                //这里可以设置一些参数，比如cookie存储、代理等等
                                httpClientBuilder.disableAuthCaching();
                                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                            }));
        }
    }


    /**
     * 带查询条件，不带分页，表示为全查询
     *
     * @param indices     索引名
     * @param includes    包含字段
     * @param excludes    不包含字段
     * @param orderName   排序字段
     * @param orderRule   排序规则true-asc false-desc
     * @param boolBuilder 查询规则
     * @return SearchHits
     */
    public SearchHits getIndexDocument(String indices,
                                       String[] includes,
                                       String[] excludes,
                                       String orderName,
                                       Boolean orderRule,
                                       BoolQueryBuilder boolBuilder) throws IOException {
        return getIndexDocumentLimit(indices, null, null,
                includes, excludes, orderName, orderRule, boolBuilder);
    }

    /**
     * 不带查询条件的查询
     *
     * @param indices   索引名
     * @param includes  包含字段
     * @param excludes  不包含字段
     * @param orderName 排序字段
     * @param orderRule 排序规则true-asc false-desc
     * @return SearchHits
     */
    public SearchHits getIndexDocumentMatchAll(String indices,
                                               String[] includes,
                                               String[] excludes,
                                               String orderName,
                                               Boolean orderRule) throws IOException {
        return getIndexDocumentLimit(indices, null, null, includes,
                excludes, orderName, orderRule, null);
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
     * @return SearchHits
     */
    public SearchHits getIndexDocumentLimit(String indices,
                                            Integer from,
                                            Integer size,
                                            String[] includes,
                                            String[] excludes,
                                            String orderName,
                                            Boolean orderRule,
                                            BoolQueryBuilder boolBuilder) throws IOException {
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
        SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
        //4、返回结果
        return searchResponse.getHits();
    }

    /**
     * 部分更新es索引内容
     *
     * @param indexName
     * @param indexID
     * @param jsonMap   更新的内容
     */
    public void updateTage(String indexName, String indexID, Map<String, Object> jsonMap) {
        UpdateRequest updateRequest = new UpdateRequest(indexName, indexID).doc(jsonMap);
        try {
            UpdateResponse updateResponse = this.client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("es更新索引内容失败：{}", e.getMessage());
        }
    }

    /**
     * 判断是否存在索引
     * @param indexName
     * @return
     */
    public boolean isExists(String indexName, String indexID) {
        GetRequest getRequest = new GetRequest(indexName, indexID);
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        boolean exists = false;
        try {
            exists = this.client.exists(getRequest, RequestOptions.DEFAULT);
        }catch (IOException e) {
            logger.error("es确认索引是否存在失败：{}", e.getMessage());
        }
        return exists;
    }

    /**
     * 创建索引
     * @param indexName
     * @param indexID
     * @param jsonMap
     * @return
     */
    public IndexResponse creatIndex(String indexName, String indexID, Map<String, Object> jsonMap) {
        IndexRequest indexRequest = new IndexRequest(indexName).id(indexID).source(jsonMap);
        IndexResponse indexResponse = null;
        try {
            indexResponse = this.client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("es创建失败：{}", e.getMessage());
        }
        return indexResponse;
    }

    /**
     * 创建索引
     * @param indexName
     * @param jsonMap
     * @return
     */
    public IndexResponse creatIndex(String indexName, Map<String, Object> jsonMap) {
        IndexRequest indexRequest = new IndexRequest(indexName).source(jsonMap);
        IndexResponse indexResponse = null;
        try {
            indexResponse = this.client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("es创建失败：{}", e.getMessage());
        }
        return indexResponse;
    }
}
