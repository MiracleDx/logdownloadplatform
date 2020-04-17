package com.log.download.platform.exception;

/**
 * DataConflictException
 * 数据已存在异常
 * @author Dongx
 * Description: 
 * Created in: 2019-01-15 10:34
 * Modified by:
 */
public class DataConflictException extends BusinessException {
	
	public DataConflictException(String msg) {
		super(msg);
	}

}
