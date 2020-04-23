package com.log.download.platform.dto;

import lombok.Data;

/**
 * QueryLogDetailDTO
 *
 * @author Dongx
 * Description:
 * Created in: 2020-03-18 14:08
 * Modified by:
 */
@Data 
public class QueryLogDetailDTO {
	//todo 增加接收脚本参数

	/**
	 * 服务编号
	 */
	private String label;

	/**
	 * ip集合
	 */
	private String[] ips;

	/**
	 * 蓝鲸需要的参数
	 */
	private String bkParam;

	/**
	 * 是否是历史查询
	 */
	private Boolean isHistory;

	/**
	 * 日志分发脚本入参
	 */
	private String flag;

	/**
	 * 容器名
	 */
	private String hostname;
}
