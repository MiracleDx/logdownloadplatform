package com.log.download.platform.dto;

import lombok.Data;

/**
 * DeploymentGroupDTO
 *
 * @author Dongx
 * Description:
 * Created in: 2020-03-16 16:33
 * Modified by:
 */
@Data
public class DeploymentGroupDTO {

	/**
	 * 项目编号
	 */
	private String projectNo;

	/**
	 * 项目名称
	 */
	private String projectName;

	/**
	 * 微服务/前台/前台应用名称
	 */
	private String serverName;

	/**
	 * 地域
	 */
	private String area;

	/**
	 * 集群
	 */
	private String cluster;

	/**
	 * 命名空间
	 */
	private String nameSpace;

	/**
	 * 应用名称
	 */
	private String applicationName;

	/**
	 * 部署组名称
	 */
	private String group;

	/**
	 * 实例所在节点IP
	 */
	private String ip;
}
