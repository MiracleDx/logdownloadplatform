package com.log.download.platform.exception;


import com.log.download.platform.response.ResponseCode;

/**
 * InternalServerException
 * 系统内部异常
 * @author Dongx
 * Description: 
 * Created in: 2019-01-15 10:35
 * Modified by:
 */
public class InternalServerException extends BusinessException {
	
	public InternalServerException(ResponseCode responseCode) {
		super(responseCode);
	}
}
