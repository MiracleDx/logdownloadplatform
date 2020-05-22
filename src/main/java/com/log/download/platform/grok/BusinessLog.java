package com.log.download.platform.grok;

import lombok.Data;

/**
 * BusinessLog
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-22 10:33
 * Modified by:
 */
@Data
public class BusinessLog extends AbstractGrokField {
	
	@GrokAttributes(order = 0, regularKey = "", errMsg = "")
	private String timestamp;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String pGlobalTraceId;
	
	@GrokAttributes(order = 2, regularKey = "", errMsg = "")
	private String pParentTraceId;
	
	@GrokAttributes(order = 3, regularKey = "", errMsg = "")
	private String pLocalTraceId;
	
	@GrokAttributes(order = 4, regularKey = "", errMsg = "")
	private String thread;
	
	@GrokAttributes(order = 5, regularKey = "", errMsg = "")
	private String level;
	
	@GrokAttributes(order = 6, regularKey = "", errMsg = "")
	private String hostName;
	
	@GrokAttributes(order = 7, regularKey = "", errMsg = "")
	private String port;
	
	@GrokAttributes(order = 8, regularKey = "", errMsg = "")
	private String serverName;
	
	@GrokAttributes(order = 9, regularKey = "", errMsg = "")
	private String startTime;
	
	@GrokAttributes(order = 10, regularKey = "", errMsg = "")
	private String method;
	
	@GrokAttributes(order = 11, regularKey = "", errMsg = "")
	private String input;
	
	@GrokAttributes(order = 12, regularKey = "", errMsg = "")
	private String output;
	
	@GrokAttributes(order = 13, regularKey = "", errMsg = "")
	private String times;
	
	@GrokAttributes(order = 14, regularKey = "", errMsg = "")
	private String message;

}
