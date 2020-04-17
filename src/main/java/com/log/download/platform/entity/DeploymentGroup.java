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
	 * 命名空间
	 */
	@ExcelProperty(value = "命名空间", index = 0)
	private String nameSpace;
	
	/**
	 * 项目名称
	 */
	@ExcelProperty(value = "命名空间描述", index = 1)
	private String projectName;

	/**
	 * 应用名称
	 */
	@ExcelProperty(value = "应用名称", index = 2)
	private String applicationName;

	/**
	 * 微服务/前台页面/前台应用名称
	 */
	@ExcelProperty(value = "应用描述", index = 3)
	private String serverName;

	/**
	 * 部署组名称
	 */
	@ExcelProperty(value = "部署组名称", index = 4)
	private String group;

	/**
	 * 集群
	 */
	@ExcelProperty(value = "集群名称", index = 5)
	private String cluster;


	/**
	 * 地域
	 */
	@ExcelProperty(value = "区域", index = 6)
	private String area;

	/**
	 * 实例所在节点IP
	 */
	@ExcelProperty(value = "虚机IP", index = 7)
	private String ip;
}
