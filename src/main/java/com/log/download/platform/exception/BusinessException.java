package com.log.download.platform.exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * BusinessException
 * 业务异常类
 * @author Dongx
 * Description:
 * Created in: 2019-01-15 10:15
 * Modified by:
 */
@Data
public class BusinessException extends RuntimeException {

	Logger logger = LoggerFactory.getLogger(BusinessException.class);

	private static final long serialVersionUID = 1L;
	
	private Integer code;
	
	private String message;
	
	public BusinessException(String msg) {
		this.message = msg;
	}
	
	public BusinessException(Integer code, String msg) {
		this.code = code;
		this.message = msg;
	}

	@Override
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	@Override
	public void printStackTrace(PrintStream s) {
		printStackTrace(new PrintWriter(s));
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		logger.info("exception occurred: {}, {}", this, super.getMessage());
	}

}
