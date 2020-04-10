package com.log.download.platform.context;

import com.log.download.platform.service.IBaseService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StrategyFactory
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-10 9:24
 * Modified by:
 */
@Component
public class StrategyFactory {

	@Resource
	Map<String, IBaseService> map = new ConcurrentHashMap<>(2);

	public IBaseService getStrategy(String component) throws Exception{
		IBaseService service = map.get(component);
		if(service == null) {
			throw new RuntimeException("no service defined");
		}
		return service;
	}
}
