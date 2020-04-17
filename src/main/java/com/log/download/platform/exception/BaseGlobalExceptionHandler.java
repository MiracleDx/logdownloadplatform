package com.log.download.platform.exception;

import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.support.ParameterInvalidItem;
import com.log.download.platform.util.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * BaseGlobalExceptionHandler
 * 基础全局异常处理类
 * @author Dongx
 * Description:
 * Created in: 2019-01-17 8:58
 * Modified by:
 */
@Slf4j
@RestControllerAdvice
public class BaseGlobalExceptionHandler {

	/**
	 * 违反约束异常
	 */
	protected ServerResponse handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
		log.error("Exception occurred, uri: {}, caused by: ", request.getRequestURI(), e);
		Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
		ConvertUtil.convertCVSetToParameterInvalidItemList(constraintViolations);
		return ServerResponse.failure(HttpStatus.BAD_REQUEST.value(), e.getMessage());
	}

	/**
	 * 处理验证参数封装错误时异常
	 */
	protected ServerResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
		log.error("Exception occurred, uri: {}, caused by: ", request.getRequestURI(), e);
		return ServerResponse.failure(HttpStatus.BAD_REQUEST.value(), e.getMessage());
	}

	/**
	 * 处理参数绑定时异常（反400错误码）
	 */
	protected ServerResponse handleBindException(BindException e, HttpServletRequest request) {
		log.error("Exception occurred, uri: {}, caused by: ", request.getRequestURI(), e);
		List<ParameterInvalidItem> parameterInvalidItemList = ConvertUtil.convertBindingResultToMapParameterInvalidItemList(e.getBindingResult());
		return ServerResponse.failure(HttpStatus.BAD_REQUEST.value(), e.getMessage(), parameterInvalidItemList);
	}

	/**
	 * 处理使用@Validated注解时，参数验证错误异常（反400错误码）
	 */
	protected ServerResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
		log.error("Exception occurred, uri: {}, caused by: ", request.getRequestURI(), e);
		List<ParameterInvalidItem> parameterInvalidItemList = ConvertUtil.convertBindingResultToMapParameterInvalidItemList(e.getBindingResult());
		return ServerResponse.failure(HttpStatus.BAD_REQUEST.value(), e.getMessage(), parameterInvalidItemList);
	}

	/**
	 * 处理通用自定义业务异常
	 */
	protected ServerResponse handleBusinessException(BusinessException e, HttpServletRequest request) {
		StackTraceElement stackTraceElement = e.getStackTrace()[0];
		log.error("Exception occurred, uri: {}, exception: {}, stackTrace: {}.{}:{}, caused by: {} ", request.getRequestURI(), e.getClass().getSimpleName(), stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber(), e.getMessage());
		return ServerResponse.failure(Optional.ofNullable(e.getCode()).orElse(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage());
	}

	/**
	 * 处理运行时系统异常（反500错误码）
	 */
	protected ServerResponse handleRuntimeException(RuntimeException e, HttpServletRequest request) {
		log.error("Exception occurred, uri: {}, caused by: {}", request.getRequestURI(), e.getMessage(), e);
		//TODO 可通过邮件、微信公众号等方式发送信息至开发人员、记录存档等操作
		return ServerResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统繁忙，请稍后重试");
	}
}
