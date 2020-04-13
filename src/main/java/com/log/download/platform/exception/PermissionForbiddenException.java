package com.log.download.platform.exception;


import com.log.download.platform.response.ResponseCode;

/**
 * PermissionForbiddenException
 * 无访问权限异常
 * @author Dongx
 * Description: 
 * Created in: 2019-01-15 10:34
 * Modified by:
 */
public class PermissionForbiddenException extends BusinessException {
	
	public PermissionForbiddenException(ResponseCode responseCode) {
		super(responseCode);
	}
	
	public PermissionForbiddenException(Integer code, String msg) {
		super(code, msg);
	}

	public PermissionForbiddenException(ResponseCode responseCode, String msg) {
		super(responseCode, msg);
	}
}
