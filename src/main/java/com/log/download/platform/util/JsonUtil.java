package com.log.download.platform.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JsonUtil
 *
 * @author Dongx
 * Description:
 * Created in: 2019-01-14 18:27
 * Modified by:
 */
@Slf4j
public class JsonUtil {

	private static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		//设置输入时忽略JSON字符串中存在而Java对象实际没有的属性
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static String object2Json(Object o) {
		if (o == null) {
			return null;
		}
		
		String s = null;

		try {
			s = mapper.writeValueAsString(o);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		return s;
	}

	public static <T> List<String> listObject2ListJson(List<T> objects) {
		if (objects == null){
			return null;
		}

		List<String> lists = new ArrayList<>();
		for (T t : objects) {
			lists.add(JsonUtil.object2Json(t));
		}

		return lists;
	}

	public static <T> List<T> listJson2ListObject(List<String> jsons, Class<T> c) {
		if (jsons == null) {
			return null;
		}
		
		List<T> ts = new ArrayList<>();
		for (String j : jsons) {
			ts.add(JsonUtil.json2Object(j, c));
		}

		return ts;
	}

	public static <T> T json2Object(String json, Class<T> c) {
		if (!StringUtils.hasLength(json)) {
			return null;
		}
		
		T t = null;
		try {
			t = mapper.readValue(json, c);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return t;
	}
}
