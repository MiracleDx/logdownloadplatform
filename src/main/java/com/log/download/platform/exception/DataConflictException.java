package com.log.download.platform.exception;

import com.log.download.platform.response.ResponseCode;

/**
 * DataConflictException
 * 数据已存在异常
 * @author Dongx
 * Description: 
 * Created in: 2019-01-15 10:34
 * Modified by:
 */
public class DataConflictException extends BusinessException {
	
	public DataConflictException(ResponseCode responseCode) {
		super(responseCode);
	}
}
