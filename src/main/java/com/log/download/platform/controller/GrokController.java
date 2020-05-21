package com.log.download.platform.controller;

import com.log.download.platform.exception.DataNotFoundException;
import com.log.download.platform.grok.AbstractGrokDebugger;
import com.log.download.platform.grok.GrokDetailDTO;
import com.log.download.platform.grok.GrokProperties;
import com.log.download.platform.grok.GrokResult;
import com.log.download.platform.response.ResponseResult;
import com.log.download.platform.util.ElasticSearchUtil;
import org.elasticsearch.common.Strings;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GrokController
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-21 09:13
 * Modified by:
 */
@ResponseResult
@RestController
public class GrokController {
	
	@Resource
	private ApplicationContext applicationContext;
	
	@Resource
	private Environment environment;
	
	
	@PostMapping("/v1/grok")
	public GrokResult grok(@RequestBody GrokDetailDTO grokDetailDTO) {
		String type = grokDetailDTO.getType();
		String beanPrefix = type.substring(0, 1).toLowerCase() + type.substring(1);
		AbstractGrokDebugger grokDebugger = (AbstractGrokDebugger) applicationContext.getBean(beanPrefix + "GrokDebugger");
		return grokDebugger.grokJudge(grokDetailDTO);
	}

	@GetMapping("/v1/reload")
	public void reload() {
		GrokProperties grokProperties = new GrokProperties();
		grokProperties.reloadProperties();
	}
	
	@GetMapping("/v1/grokLog/{status}")
	public List<Map<String, Object>> grokLog(@PathVariable String status) {
		ElasticSearchUtil elasticSearchUtil = ElasticSearchUtil.getInstance();
		elasticSearchUtil.init(environment, new String[]{"10.155.208.144:9200"});
		String[] includes = new String[]{"*"};
		String[] excludes = Strings.EMPTY_ARRAY;
		SearchHits hits = null;
		
		String index = "grok_" + (Boolean.parseBoolean(status) ? "success" : "failed");
		
		try {
			hits = elasticSearchUtil.getIndexDocumentMatchAll(index, includes, excludes, "grokTime", false);
		} catch (IOException e) {
			throw new DataNotFoundException(String.format("ElasticSearch Index %s Not Found", index));
		}

		SearchHit[] searchHits = hits.getHits();
		List<Map<String, Object>> list = new ArrayList<>(searchHits.length);
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> source = searchHit.getSourceAsMap();
			list.add(source);
		}
		return list;
	}
	
}
