package com.log.download.platform.grok;

import cn.hutool.core.io.FileUtil;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * GrokProperites
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-20 11:08
 * Modified by:
 */
public class GrokProperties {
	
	private Map<String, String> map = new ConcurrentHashMap<>();
	
	private void readProperties() {
		List<String> list = FileUtil.readLines("C:/grok-patterns", "utf-8");
		for (String str : list) {
			// 过滤掉注释字段
			if (!str.contains("# ") && !str.isEmpty()) {
				String[] kv = str.split(" ");
				synchronized (map) {
					map.put(kv[0], kv[1]);
				}
			}
		}
		String reg = "%\\{[A-Z|0-9|_|:a-z]+\\}";
		Pattern pattern = Pattern.compile(reg);
		List<String> collect = list.stream()
				.filter(str -> !str.contains("# ") && !str.isEmpty())
				.map(str -> {
					while (str.contains("%{")) {
						Matcher matcher = pattern.matcher(str);
						while (matcher.find()) {
							String group = matcher.group();
							String existsKey = null;
							if (group.contains(":")) {
								existsKey = group.substring(group.indexOf("{") + 1, group.lastIndexOf(":"));
							} else {
								existsKey = group.substring(group.indexOf("{") + 1, group.lastIndexOf("}"));
							}
							str = str.replace(group, map.get(existsKey));
						}
					}
					return str;
				}).collect(Collectors.toList());
		
		// 重新赋值
		for (String str : collect) {
			String[] kv = str.split(" ", 2);
			synchronized (map) {
				map.put(kv[0], kv[1]);
			}
		}

		//map.forEach((k, v) -> {
		//	System.out.println(k + " : " + v);
		//});
	}
	
	public void init() {
		if (map.size() == 0) {
			readProperties();
		}
	};
	
	private Map<String, String> getPropertiesMap() {
		init();
		return map;
	}

	public Map<String, String> getProperties() {
		return getPropertiesMap();
	}
	
	public String getProperty(String key) {
		return getPropertiesMap().get(key);
	}
	
	public Map<String, String> reloadProperties() {
		synchronized (map) {
			map.clear();
		}
		return getPropertiesMap();
	}
	
	public void replaceReg(String reg) {
		Pattern pattern = Pattern.compile(reg);
		map.forEach((k, v) -> {
			// 包含logstash组合正则
			if (v.contains("%{")) {
				Matcher matcher = pattern.matcher(v);
				while (matcher.find()) {
					String group = matcher.group();
					String existsKey = null;
					if (group.contains(":")) {
						existsKey = group.substring(group.indexOf("{") + 1, group.lastIndexOf(":"));
					} else {
						existsKey = group.substring(group.indexOf("{") + 1, group.lastIndexOf("}"));
					}

					v = v.replace(group, map.get(existsKey));
				}
				map.put(k, v);
			}
		});
	}

	public static void main(String[] args) {
		new GrokProperties().readProperties();
	}
}
