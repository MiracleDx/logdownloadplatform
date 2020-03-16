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
	
	@ExcelProperty(value = "命名空间", index = 0)
	private String nameSpace;

	@ExcelProperty(value = "微服务编码", index = 1)
	private String serverCode;

	@ExcelProperty(value = "部署组", index = 2)
	private String group;
}
