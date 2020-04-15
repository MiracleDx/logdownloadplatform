package com.log.download.platform.util;

import com.log.download.platform.exception.DataConflictException;
import com.log.download.platform.response.ResponseCode;

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
	//todo 判断有问题，需要增加判断条件，判断网关文件
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
		server_general
	}
}
