package com.log.download.platform.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * GatewayGroup
 *
 * @author Dongx
 * Description:
 * Created in: 2020-03-19 15:36
 * Modified by:
 */
@Data
public class GatewayGroup {

	/**
	 * 项目编号
	 */
	@ExcelProperty(value = "项目编号", index = 0)
	private String projectNo;

	/**
	 * 项目名称
	 */
	@ExcelProperty(value = "项目名称", index = 1)
	private String projectName;

	/**
	 * 微服务网关
	 */
	@ExcelProperty(value = "微服务网关", index = 2)
	private String serverName;

	/**
	 * 地域
	 */
	@ExcelProperty(value = "地域", index = 3)
	private String area;

	/**
	 * 微服务网关名称
	 */
	@ExcelProperty(value = "微服务网关名称", index = 4)
	private String applicationName;

	/**
	 * 集群名称
	 */
	@ExcelProperty(value = "集群名称", index = 5)
	private String cluster;

	/**
	 * 集群名称
	 */
	@ExcelProperty(value = "实例所在节点IP", index = 6)
	private String ip;
}
