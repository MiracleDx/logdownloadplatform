package com.log.download.platform.exception;

/**
 * NotImplementedException
 * 服务器不支持当前请求所需要的某个功能
 * @author Dongx
 * Description:
 * Created in: 2020-04-23 10:07
 * Modified by:
 */
public class NotImplementedException extends BusinessException {
	
	public NotImplementedException(String msg) {
		super(msg);
	}
}
