package com.log.download.platform.response;

import com.log.download.platform.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;

/**
 * ResponseResultHandler
 *
 * @author Dongx
 * Description: 接口响应体处理器
 * Created in: 2019-01-14 18:23
 * Modified by:
 */
@RestControllerAdvice
public class ResponseResultHandler implements ResponseBodyAdvice<Object> {

	/**
	 * 返回值类型
	 */
	private static final String RETURN_TYPE = "void";
	
	/**
	 * 判断哪些类需要拦截
	 * @param returnType
	 * @param converterType
	 * @return
	 */
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		Method method = returnType.getMethod();
		ResponseResult annClazz = null;
		ResponseResult annMethod = null;
		// 获取声明类注解
		annClazz = method.getDeclaringClass().getAnnotation(ResponseResult.class);
		if (annClazz == null) {
			// 获取方法注解
			annMethod = method.getAnnotation(ResponseResult.class);
		}
		return annClazz != null || annMethod != null;
	}

	/**
	 * 
	 * @param body
	 * @param returnType
	 * @param selectedContentType
	 * @param selectedConverterType
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
			
		if (StringUtils.equalsIgnoreCase(returnType.getParameterType().getName(), RETURN_TYPE)) {
			return ServerResponse.success(body);
		}
			
		if (body instanceof ServerResponse) {
			return body;
		}
	
		if (body instanceof String) {
			return JsonUtil.object2Json(ServerResponse.success(body));
		}
		
		if (body == null) {
			return ServerResponse.failure(ResponseCode.DATA_NOT_FOUND.getCode(), ResponseCode.DATA_NOT_FOUND.getMessage());
		}
		return ServerResponse.success(body);
	}
}

