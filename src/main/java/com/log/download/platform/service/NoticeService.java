package com.log.download.platform.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.log.download.platform.dto.NoticeDTO;
import com.log.download.platform.util.ElasticSearchUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * NoticeService
 * 公告功能
 * @author Dongx
 * Description:
 * Created in: 2020-04-14 15:28
 * Modified by:
 */
@Slf4j
@Service
public class NoticeService {

	private static final List<String> NOTICE = new ArrayList<>();
	
	@Value("${notice.location}")
	private String noticeLocation;
	
	private final String index = "logdownloader-notice";
	
	private String indexId = "1";
	
	private String[] esUrl = {"10.157.208.188:9200"};

	@Resource
	private ElasticsearchService elasticsearchService;

	@Resource
	private Environment environment;
	
	@Resource
	private ObjectMapper objectMapper;

	/**
	 * 编辑公告栏
	 * @param noticeDTO
	 */
	public void editNotice(NoticeDTO noticeDTO) {
		List<String> msg = noticeDTO.getMessage();
		synchronized (NOTICE) {
			NOTICE.clear();
			NOTICE.addAll(msg);
			//FileUtil.writeString(noticeDTO.getMessage().get(0), noticeLocation, "utf-8");
		}

		ForkJoinPool.commonPool().execute(() -> {
			try (RestHighLevelClient client = ElasticSearchUtil.getInstance().getClient(environment, esUrl)) {
				Map<String, Object> jsonMap = new HashMap<>(1);
				jsonMap.put("msg", msg.get(0));
				jsonMap.put("@timestamp", new Date());
				elasticsearchService.updateTage(index, indexId, jsonMap, client);
			} catch (IOException e) {
				log.error("es 写入异常：{}", e.getMessage());
			}
		});
	}

	/**
	 * 获取公告栏内容
	 * @return
	 */
	public List<String> getNotice() {
		if (!new File(noticeLocation).exists()) {
			return new ArrayList<>();
		}

		String[] includes = {"msg", "@timestamp"};
		String[] excludes = {};
		try (RestHighLevelClient client = ElasticSearchUtil.getInstance().getClient(environment, esUrl)) {
			if (!elasticsearchService.isExists(index, indexId, client)){
				Map<String, Object> map = new HashMap<>();
				map.put("msg", "欢迎使用日志下载平台");
				map.put("@timestamp", new Date());
				elasticsearchService.creatIndex(index, indexId, map, client);
			}
			// 查询
			SearchHits hits = elasticsearchService.getIndexDocumentLimit(
					index, 0, 1, includes, excludes, "@timestamp",
					false, null, client);
			for (SearchHit hit : hits) {
				indexId = hit.getId();
				String str = hit.getSourceAsString();
				cn.hutool.json.JSONObject jsonObject = new JSONObject(str);
				String msg = jsonObject.get("msg").toString();
				if (NOTICE.size() == 0) {
					NOTICE.add(msg);	
				}
			}
		} catch (IOException e) {
			log.error("查询 es 异常：{}", e.getMessage());
		}
		
		return NOTICE;
	}
}
