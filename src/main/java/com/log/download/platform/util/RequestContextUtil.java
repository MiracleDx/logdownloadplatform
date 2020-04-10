package com.log.download.platform.util;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;

/**
 * RequestContextUtil
 *
 * @author Dongx
 * Description:
 * Created in: 2019-01-14 17:09
 * Modified by:
 */
public class RequestContextUtil {

	public static HttpServletRequest getRequest() {
		return getRequestAttributes().getRequest();
	}

	public static HttpServletResponse getResponse() {
		return getRequestAttributes().getResponse();
	}

	public static HttpSession getSession() {
		return getRequest().getSession();
	}

	public static ServletRequestAttributes getRequestAttributes() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
	}

	public static ServletContext getServletContext() {
		return Objects.requireNonNull(ContextLoader.getCurrentWebApplicationContext()).getServletContext();
	}
}
