package com.log.download.platform.controller;

import com.log.download.platform.grok.AbstractGrokDebugger;
import com.log.download.platform.grok.GrokDetailDTO;
import com.log.download.platform.grok.GrokProperties;
import com.log.download.platform.grok.GrokResult;
import com.log.download.platform.response.ResponseResult;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
	
}
