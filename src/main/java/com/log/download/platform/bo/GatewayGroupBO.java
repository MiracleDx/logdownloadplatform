package com.log.download.platform.bo;

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
public class GatewayGroupBO {

	private String projectNo;

	private String projectName;

	private String applicationName;

	private String area;

	private String serverName;

	private String cluster;

	private String ip;
}
