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
}
