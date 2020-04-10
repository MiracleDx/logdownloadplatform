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

	@Override
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ConstraintViolationException.class)
	public ServerResponse handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
		return super.handleConstraintViolationException(e, request);
	}

	@Override
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ServerResponse handleConstraintViolationException(HttpMessageNotReadableException e, HttpServletRequest request) {
		return super.handleConstraintViolationException(e, request);
	}

	@Override
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(BindException.class)
	public ServerResponse handleBindException(BindException e, HttpServletRequest request) {
		return super.handleBindException(e, request);
	}

	@Override
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ServerResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
		return super.handleMethodArgumentNotValidException(e, request);
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
