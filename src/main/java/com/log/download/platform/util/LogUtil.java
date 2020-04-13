package com.log.download.platform.util;

import com.log.download.platform.exception.DataConflictException;
import com.log.download.platform.response.ResponseCode;

/**
 * LogUtil
 * 判断日志类型
 * @author Dongx
 * Description:
 * Created in: 2020-04-13 09:39
 * Modified by:
 */
public class LogUtil {
	
	private static final String TSF_DEFAULT = "tsf_default";
	
	private static final String TSF_GATEWAY = "gate_default";
	
	private static final String MSGW = "msgw";
	
	private LogUtil() {
		
	}

	private static class SingletonInstance {
		private static final LogUtil INSTANCE = new LogUtil();
	}

	public static LogUtil getInstance() {
		return LogUtil.SingletonInstance.INSTANCE;
	}

	/**
	 * 判断日志类型
	 * @param path
	 * @return
	 */
	public LogEnum logType(String path) {
		if (path.contains(MSGW)) {
			return LogEnum.gateway;
		} else {
			return LogEnum.server;
		}
	}

	/**
	 * 判断日志落盘方式
	 * @param path
	 * @return
	 */
	public LogEnum placeWay(String path) {
		if (logType(path) == LogEnum.gateway) {
			if (path.contains(TSF_GATEWAY)) {
				return LogEnum.gateway_container;
			} else {
				return LogEnum.gateway_general;
			}
		} else if (logType(path) == LogEnum.server) {
			if (path.contains(TSF_DEFAULT)) {
				return LogEnum.server_container;
			} else {
				return LogEnum.server_general;
			}
		} else {
			throw new DataConflictException(ResponseCode.DATA_IS_WRONG);
		}
	}
	
	public enum LogEnum {

		/**
		 * 网关
		 */
		gateway,
		/**
		 * 网关 容器
		 */
		gateway_container,
		/**
		 * 网关 已落盘
		 */
		gateway_general,

		/**
		 * 微服务
		 */
		server,
		/**
		 * 微服务 容器
		 */
		server_container,
		/**
		 * 微服务 已落盘
		 */
		server_general
	}
}
