package com.log.download.platform.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

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
                            })
            );
        }
    }
}
