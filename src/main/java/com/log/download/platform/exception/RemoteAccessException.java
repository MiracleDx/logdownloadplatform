package com.log.download.platform.exception;


import com.log.download.platform.response.ResponseCode;

/**
 * RemoteAccessException
 * 远程访问时错误异常
 * @author Dongx
 * Description: 
 * Created in: 2019-01-15 10:35
 * Modified by:
 */
public class RemoteAccessException extends BusinessException {
	
	public RemoteAccessException(ResponseCode responseCode) {
		super(responseCode);
	}
}
