package com.log.download.platform.exception;

import com.log.download.platform.response.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

/**
 * CustomerExceptionHandler
 * 自定义异常处理类
 * @author Dongx
 * Description:
 * Created in: 2019-01-06 20:00
 * Modified by:
 */
@RestControllerAdvice
public class CustomerExceptionHandler extends BaseGlobalExceptionHandler {

	/**
	 * 违反约束异常
	 */
	@Override
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ConstraintViolationException.class)
	public ServerResponse handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
		return super.handleConstraintViolationException(e, request);
	}

	/**
	 * 处理验证参数封装错误时异常
	 */
	@Override
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ServerResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
		return super.handleHttpMessageNotReadableException(e, request);
	}

	/**
	 * 处理参数绑定时异常（反400错误码）
	 */
	@Override
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(BindException.class)
	public ServerResponse handleBindException(BindException e, HttpServletRequest request) {
		return super.handleBindException(e, request);
	}

	/**
	 * 处理使用@Validated注解时，参数验证错误异常（反400错误码）
	 */
	@Override
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ServerResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
		return super.handleMethodArgumentNotValidException(e, request);
	}

	/**
	 * 处理数据查找不到异常
	 * @param e
	 * @param request
	 * @return
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(DataNotFoundException.class)
	public ServerResponse handleDataNotFoundException(DataNotFoundException e, HttpServletRequest request) {
		e.setCode(HttpStatus.NOT_FOUND.value());
		return super.handleBusinessException(e, request);
	}

	/**
	 * 处理数据冲突异常
	 * @param e
	 * @param request
	 * @return
	 */
	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler(DataConflictException.class)
	public ServerResponse handleDataConflictException(DataConflictException e, HttpServletRequest request) {
		e.setCode(HttpStatus.CONFLICT.value());
		return super.handleBusinessException(e, request);
	}

	/**
	 * 处理权限错误
	 * @param e
	 * @param request
	 * @return
	 */
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(PermissionForbiddenException.class)
	public ServerResponse handlePermissionForbiddenException(PermissionForbiddenException e, HttpServletRequest request) {
		e.setCode(HttpStatus.FORBIDDEN.value());
		return super.handleBusinessException(e, request);
	}

	/**
	 * 处理远程调用异常
	 * @param e
	 * @param request
	 * @return
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(RemoteAccessException.class)
	public ServerResponse handleRemoteAccessException(RemoteAccessException e, HttpServletRequest request) {
		return super.handleBusinessException(e, request);
	}

	/**
	 * 处理请求超时异常
	 * @param e
	 * @param request
	 * @return
	 */
	@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
	@ExceptionHandler(RequestTimeoutException.class)
	public ServerResponse handleRequestTimeoutException(RequestTimeoutException e, HttpServletRequest request) {
		e.setCode(HttpStatus.REQUEST_TIMEOUT.value());
		return super.handleBusinessException(e, request);
	}

	@Override
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(BusinessException.class)
	public ServerResponse handleBusinessException(BusinessException e, HttpServletRequest request) {
		return super.handleBusinessException(e, request);
	}

	@Override
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(RuntimeException.class)
	public ServerResponse handleRuntimeException(RuntimeException e, HttpServletRequest request) {
		return super.handleRuntimeException(e, request);
	}
}
