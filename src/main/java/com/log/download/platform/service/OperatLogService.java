package com.log.download.platform.service;

import com.log.download.platform.dto.OperatLogDTO;
import com.log.download.platform.util.ElasticSearchUtil;
import com.log.download.platform.vo.OperatLogVO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-05-28 16:43
 **/
@Slf4j
@Service
public class OperatLogService {

    @Resource
    private Environment environment;

    public OperatLogDTO getOperatingLog(long startDate, long endDate){
        LocalDateTime time = LocalDateTime.now();
        List<OperatLogVO> operatLogVOS = new ArrayList<>();
        OperatLogVO operatLogVO;
        HashMap<String, Integer> map = new HashMap<>(16);
        ElasticSearchUtil es = ElasticSearchUtil.getInstance();
        es.init(environment, new String[]{"10.155.208.144:9200"});
        try {
            QueryBuilder queryBuilder = QueryBuilders.rangeQuery("operattime")
                    .from(LocalDateTime.ofEpochSecond(startDate, 0, ZoneOffset.ofHours(8)))
                    .to(LocalDateTime.ofEpochSecond(endDate, 0, ZoneOffset.ofHours(8)))
                    .includeLower(true).includeUpper(true);
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(queryBuilder);
            SearchHits searchHits = es.getIndexDocument("operatinglog",
                    new String[]{"operattime", "userip", "operatstate"}, new String[]{""}, "operattime",
                    false, boolQueryBuilder);
            for (SearchHit searchHit : searchHits) {
                operatLogVO = new OperatLogVO();
                operatLogVO.setOperatstate((String) searchHit.getSourceAsMap().get("operatstate"));
                operatLogVO.setOperattime((String) searchHit.getSourceAsMap().get("operattime"));
                operatLogVO.setUserip((String) searchHit.getSourceAsMap().get("userip"));
                operatLogVOS.add(operatLogVO);
                map.compute(operatLogVO.getOperatstate(), (k, v) -> {
                    if (v == null) {
                        return 1;
                    }
                    return v + 1;
                });
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return OperatLogDTO.builder().operatLogVOS(operatLogVOS).operats(map).build();
    }
}
