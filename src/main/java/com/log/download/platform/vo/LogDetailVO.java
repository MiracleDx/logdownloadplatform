package com.log.download.platform.vo;

import lombok.Data;

import java.util.Objects;

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

	/**
	 * 日志分发脚本入参
	 */
	private String param;
	
	private String hostname;

	@Override
	public boolean equals(Object o){
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LogDetailVO logDetailVO = (LogDetailVO)o;

		return path.equals(logDetailVO.path) && createTime.equals(logDetailVO.createTime) &&
				size.equals(logDetailVO.size) && label.equals(logDetailVO.label) &&
				hostname.equals(logDetailVO.hostname);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, createTime, size, label, hostname);
	}
	
}
