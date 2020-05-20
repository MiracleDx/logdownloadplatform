package com.log.download.platform.grok;

import lombok.Data;

/**
 * GrokDetailDTO
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-19 15:08
 * Modified by:
 */
@Data
public class GrokDetailDTO {

	/**
	 * 项目组
	 */
	private String team;

	/**
	 * 校验的日志类型
	 */
	private String type;

	/**
	 * 日志文本
	 */
	private String content;
	
}
