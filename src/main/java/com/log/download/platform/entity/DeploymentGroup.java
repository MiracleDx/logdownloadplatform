package com.log.download.platform.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * DeploymentGroup
 * 部署组信息实体
 * @author Dongx
 * Description:
 * Created in: 2020-03-13 10:54
 * Modified by:
 */
@Data
public class DeploymentGroup {

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
	 * 微服务/前台页面/前台应用名称
	 */
	@ExcelProperty(value = "微服务/前台页面/前台应用名称", index = 2)
	private String serverName;

	/**
	 * 地域
	 */
	@ExcelProperty(value = "地域", index = 3)
	private String area;

	/**
	 * 集群
	 */
	@ExcelProperty(value = "集群", index = 4)
	private String cluster;

	/**
	 * 命名空间
	 */
	@ExcelProperty(value = "命名空间", index = 5)
	private String nameSpace;

	/**
	 * 应用名称
	 */
	@ExcelProperty(value = "应用名称", index = 6)
	private String applicationName;

	/**
	 * 部署组名称
	 */
	@ExcelProperty(value = "部署组名称", index = 7)
	private String group;

	/**
	 * 实例所在节点IP
	 */
	@ExcelProperty(value = "实例所在节点IP", index = 8)
	private String ip;
}
