package com.log.download.platform.vo;

import lombok.Data;

/**
 * LogDetailVO
 * 
 * @author Dongx
 * Description:
 * Created in: 2020-03-16 16:12
 * Modified by:
 */
@Data
public class LogDetailVO {
	
	private String ip;

	private Integer id;

	private String path;
	
	private String createTime;

	private String label;

	private Double size;
	
	private String unit;

	private Boolean mirror;

	private String flag;
	
}
