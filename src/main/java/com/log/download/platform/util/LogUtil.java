package com.log.download.platform.util;

import com.log.download.platform.exception.DataConflictException;

/**
 * LogUtil
 * 判断日志类型及路径
 * @author Dongx
 * Description:
 * Created in: 2020-04-13 09:39
 * Modified by:
 */
public class LogUtil {
	
	private static final String TSF_DEFAULT = "tsf_default";
	
	private static final String TSF_GATEWAY = "tsf-gateway";
	
	private static final String MSGW = "msgw";

	private static final String SERVER = "/log/";
	
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
		if (path.contains(TSF_GATEWAY) || path.contains(MSGW)) {
			return LogEnum.gateway;
		} else if (path.contains(TSF_DEFAULT) || path.contains(SERVER)) {
			return LogEnum.server;
		} else {
			return LogEnum.path_null;
		}
	}

	/**
	 * 判断日志落盘方式
	 * @param path
	 * @return
	 */
	public LogEnum placeWay(String path) {
		LogEnum logEnum = logType(path);
		if (logEnum == LogEnum.gateway) {
			if (path.contains(TSF_GATEWAY)) {
				return LogEnum.gateway_container;
			} else {
				return LogEnum.gateway_general;
			}
		} else if (logEnum == LogEnum.server) {
			if (path.contains(TSF_DEFAULT)) {
				return LogEnum.server_container;
			} else {
				return LogEnum.server_general;
			}
		} else {
			throw new DataConflictException("落盘方式未找到, 落盘路径为：" + path);
		}
	}


	/**
	 * 处理容器日志路径
	 *
	 * @param path tmp[1]=中心名称  c014
	 *             tmp[2]=应用名称  01014020
	 *             tmp[3]=分公司编码  3300
	 *             tmp[4]=部署组id  1
	 * @return
	 */
	public String processingCvmPath(String path) {
		LogEnum logEnum = placeWay(path);
		
		switch (logEnum) {
			case server_general:
				break;
			case server_container:
				String[] temp = path.split("-");
				if (!path.contains("sys_log.log")) {
					path = path.replace("/data/tsf_default/logs", "/log/" + temp[1] + "-" + temp[2] + "-" + temp[3] + "-" + temp[4]);
				}
				break;
			case gateway_general:
				break;
			case gateway_container:
				break;
			default:
				break;
		}
		
		return path;
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
		server_general,
		/**
		 * 为空
		 */
		path_null
	}

	/**
	 * 解析微服务日志
	 * @return
	 */
	public String praseServerLogDetail(String[] logArr) {
		return logArr[0];
	}

	/**
	 * 解析微服务网关日志
	 * @return
	 */
	public String praseGatewayLogDetail(String[] logArr) {
		return logArr[1];
	}
}
