package com.log.download.platform.grok;

import lombok.Data;

/**
 * ErrorLog
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-22 10:48
 * Modified by:
 */
@Data
public class ErrorLog extends AbstractGrokField {
	
	@GrokAttributes(order = 0, regularKey = "", errMsg = "")
	private String timestamp;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String thread;
	
	@GrokAttributes(order = 2, regularKey = "", errMsg = "")
	private String level;
	
	@GrokAttributes(order = 3, regularKey = "", errMsg = "")
	private String hostName;
	
	@GrokAttributes(order = 4, regularKey = "", errMsg = "")
	private String port;
	
	@GrokAttributes(order = 5, regularKey = "", errMsg = "")
	private String serverName;
	
	@GrokAttributes(order = 6, regularKey = "", errMsg = "")
	private String method;
	
	@GrokAttributes(order = 7, regularKey = "", errMsg = "")
	private String stackTrace;
	
	@GrokAttributes(order = 8, regularKey = "", errMsg = "")
	private String message;
	
	@GrokAttributes(order = 9, regularKey = "", errMsg = "")
	private String pGlobalTraceId;
	
	@GrokAttributes(order = 10, regularKey = "", errMsg = "")
	private String pParentTraceId;
	
	@GrokAttributes(order = 11, regularKey = "", errMsg = "")
	private String pLocalTraceId;

}
