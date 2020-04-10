package com.log.download.platform.support;

import lombok.Builder;
import lombok.Data;

/**
 * ParameterInvalidItem
 * 参数校验信息
 * @author Dongx
 * Description:
 * Created in: 2019-01-17 9:27
 * Modified by:
 */
@Data
@Builder
public class ParameterInvalidItem {
	
	private String filedName;
	
	private String errorMessage;
}
