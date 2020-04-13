package com.log.download.platform.exception;

import com.log.download.platform.response.ResponseCode;

/**
 * CustomizeException
 * 用户自定义信息异常
 * @author Dongx
 * Description: 
 * Created in: 2019-04-30 10:01
 * Modified by:
 */
public class CustomizeException extends BusinessException {
	
	public CustomizeException(Integer code, String msg) {
		super(code, msg);
	}

	public CustomizeException(ResponseCode responseCode) {
		super(responseCode);
	}

	public CustomizeException(ResponseCode responseCode, String msg) {
		super(responseCode, msg);
	}
}
