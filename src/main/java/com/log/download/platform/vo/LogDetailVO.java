package com.log.download.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LogDetailVO
 * 
 * @author Dongx
 * Description:
 * Created in: 2020-03-16 16:12
 * Modified by:
 */
@Data
public class LogDetailVO implements Comparable<LogDetailVO>{
	
	private String ip;

	private Integer id;

	private String path;
	
	private String createTime;

	private String label;

	private String size;

	private Boolean mirror;

	@Override
	public int compareTo(LogDetailVO logDetailVO) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		if (LocalDateTime.parse(this.createTime, df).isAfter(LocalDateTime.parse(logDetailVO.createTime, df))) {
			return 1;
		}else if (this.createTime == logDetailVO.createTime){
			return 0;
		}else {
			return -1;
		}
	}
}
