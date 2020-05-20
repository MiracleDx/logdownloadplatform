package com.log.download.platform.grok;

import com.log.download.platform.util.ElasticSearchUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public void grokJudge(GrokDetailDTO dto) {
		// 初始化es客户端
		ElasticSearchUtil elasticSearchUtil = ElasticSearchUtil.getInstance();
		elasticSearchUtil.init(environment, new String[]{"10.155.208.144:9200"});
		
		// 组装参数
		LocalDateTime now = LocalDateTime.now();
		Map<String, Object> params = new HashMap<>();
		String index = "";
		params.put("team", dto.getTeam());
		params.put("type", dto.getType());
		params.put("content", dto.getContent());
		params.put("grokTime", now);

		Class<?> clazz = null;
		try {
			clazz = Class.forName(dto.getType());
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("unknown log type");
		}

		GrokResult grokResult = grok(dto.getContent(), clazz);
		if (grokResult.getStatus()) {
			index = "grok_success";
		} else {
			index = "grok_failed";
		}
		params.put("grokResult", grokResult);
		elasticSearchUtil.creatIndex(index, params);
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
		String[] contents = Arrays.stream(split).map(str -> str.replace("\\[", "")).toArray(String[]::new);

		GrokResult result = new GrokResult();
		List<ErrorMsg> errorMsgs = new ArrayList<>();

		// 获取所有字段
		Arrays.stream(clazz.getDeclaredFields())
				// 取到有指定注解的字段
				.filter(field -> field.isAnnotationPresent(GrokAttributes.class))
				// 根据order排序
				.sorted(Comparator.comparingInt(field -> field.getAnnotation(GrokAttributes.class).order()))
				// 设置访问私有字段
				.peek(field -> field.setAccessible(true))
				.forEach(field -> {
					GrokAttributes grokAttributes = field.getAnnotation(GrokAttributes.class);
					// 获取字段顺序
					int order = grokAttributes.order();
					// 获取正则Key
					String regularKey = grokAttributes.regularKey();
					// 注解中没有声明正则Key就取字段名称
					if (StringUtils.isBlank(regularKey)) {
						regularKey = field.getName();
					}

					// todo 获取正则 获取不到的使用默认正则
					String regular = "";
					// 对应log片段
					String log = contents[order];
					// 匹配正则
					Pattern pattern = Pattern.compile(regular);
					Matcher matcher = pattern.matcher(log);
					if (!matcher.matches()) {
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
