package com.log.download.platform.grok;

import cn.hutool.json.JSONUtil;
import com.log.download.platform.util.ElasticSearchUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.*;

/**
 * GrokDebugger
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-19 14:19
 * Modified by:
 */
public abstract class AbstractGrokDebugger {
	
	@Resource
	private Environment environment;

	/**
	 * 解析日志
	 * @param dto
	 */
	public GrokResult grokJudge(GrokDetailDTO dto) {
		// 初始化es客户端
		ElasticSearchUtil elasticSearchUtil = ElasticSearchUtil.getInstance();
		elasticSearchUtil.init(environment, new String[]{"10.155.208.144:9200"});
		
		// 组装参数
		Map<String, Object> params = new HashMap<>();
		String index = "";
		params.put("team", dto.getTeam());
		params.put("type", dto.getType());
		params.put("content", dto.getContent());
		params.put("grokTime", Instant.now().toEpochMilli());

		Class<?> clazz = null;
		try {
			clazz = Class.forName(String.format("com.log.download.platform.grok.%s", dto.getType()));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(String.format("unknown log type: %s", dto.getType()));
		}

		GrokResult grokResult = grok(dto.getContent(), clazz);
		if (grokResult.getStatus()) {
			index = "grok_success";
		} else {
			index = "grok_failed";
		}

		
		params.put("grokResult", JSONUtil.parse(grokResult));
		elasticSearchUtil.creatIndex(index, params);
		return grokResult;
	}

	/**
	 * 获取日志范例
	 * @return
	 */
	public abstract String logExample();

	/**
	 * grok解析
	 * @param content
	 * @return
	 */
	public GrokResult grok(String content, Class<?> clazz) {
		if (StringUtils.isBlank(content)) {
			throw new IllegalArgumentException("log content must not null");
		}

		String[] split = content.split("\\] \\[", 0);
		String[] contents = Arrays.stream(split).map(str -> str.replaceAll("\\[", "")).toArray(String[]::new);

		GrokResult result = new GrokResult();
		List<ErrorMsg> errorMsgs = new ArrayList<>();

		GrokProperties grokProperties = new GrokProperties();
		// getDeclaredFields获取所有字段
		Arrays.stream(clazz.getDeclaredFields())
				// 判断字段是否有GrokAttributes注解，从而取到有注解的字段
				.filter(field -> field.isAnnotationPresent(GrokAttributes.class))
				// 根据注解中的order的大小排序
				.sorted(Comparator.comparingInt(field -> field.getAnnotation(GrokAttributes.class).order()))
				// 设置访问私有字段
				.peek(field -> field.setAccessible(true))
				.forEach(field -> {
					GrokAttributes grokAttributes = field.getAnnotation(GrokAttributes.class);
					// 获取字段顺序
					int order = grokAttributes.order();
					// 获取正则Key
					String regularKey = grokAttributes.regularKey().toUpperCase();
					// 注解中没有声明正则Key就取字段名称
					if (StringUtils.isBlank(regularKey)) {
						regularKey = field.getName();
					}

					// 获取正则
					String regular = grokProperties.getProperty(regularKey);

					// 获取以字段名称为key的正则
					if (regular == null) {
						regular = grokProperties.getProperty(field.getName());
					}
					
					// 获取不到使用默认正则
					if (regular == null) {
						regular = grokProperties.getProperty("DEFAULT");
					}
					
					// 对应log片段 order 是从1开始排序
					String log = contents[order - 1];
					// 匹配正则
					if (!log.matches(regular)) {
						ErrorMsg errorMsg = new ErrorMsg();
						errorMsg.setLogSnippet(log);
						errorMsg.setMsg(grokAttributes.errMsg());
						errorMsgs.add(errorMsg);
					}
				});
		boolean flag = false;
		if (errorMsgs.size() == 0) {
			flag = true;
		} else {
			result.setErrorMsgs(errorMsgs);
		}
		result.setStatus(flag);
		return result;
	}
}
