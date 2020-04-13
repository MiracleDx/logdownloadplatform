package com.log.download.platform.exception;

import com.log.download.platform.response.ResponseCode;

/**
 * DataNotFoundException
 * 数据未找到异常
 * @author Dongx
 * Description: 
 * Created in: 2019-01-15 10:34
 * Modified by:
 */
public class DataNotFoundException extends BusinessException {
	
	public DataNotFoundException(ResponseCode responseCode) {
		super(responseCode);
	}

	public DataNotFoundException(Integer code, String msg) {
		super(code, msg);
	}

	public DataNotFoundException(ResponseCode responseCode, String msg) {
		super(responseCode, msg);
	}
}
