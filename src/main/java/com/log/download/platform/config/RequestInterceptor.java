package com.log.download.platform.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * RequestInterceptor
 * 记录唯一请求号
 * @author Dongx
 * Description:
 * Created in: 2020-04-10 16:05
 * Modified by:
 */
@Configuration
public class RequestInterceptor implements WebMvcConfigurer {

	private static final String REQUEST_ID = "requestId";

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		HandlerInterceptor handlerInterceptor = new HandlerInterceptor() {
			@Override
			public boolean preHandle(HttpServletRequest request,
									 HttpServletResponse response, Object handler) throws Exception {
				//生产请求id
				String requestId = UUID.randomUUID().toString().replaceAll("-", "");
				MDC.put(REQUEST_ID, requestId);
				return true;
			}

			@Override
			public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

			}

			@Override
			public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
				//销毁请求id
				MDC.clear();
			}
		};
		registry.addInterceptor(handlerInterceptor).addPathPatterns("/**");
	}
}