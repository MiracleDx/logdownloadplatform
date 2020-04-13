package com.log.download.platform.exception;

import com.log.download.platform.response.ResponseCode;
import lombok.Getter;
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
@Getter
public class BusinessException extends RuntimeException {

	Logger logger = LoggerFactory.getLogger(BusinessException.class);

	private static final long serialVersionUID = 1L;
	
	private Integer code;
	
	private String message;
	
	private ResponseCode responseCode;
	
	public BusinessException(Integer code, String msg) {
		this.code = code;
		this.message = msg;
	}

	public BusinessException(ResponseCode responseCode) {
		this.code = responseCode.code();
		this.message = responseCode.message();
	}

	public BusinessException(ResponseCode responseCode, String msg) {
		this.code = responseCode.code();
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
