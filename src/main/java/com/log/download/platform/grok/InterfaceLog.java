package com.log.download.platform.grok;

import lombok.Data;

/**
 * InterfaceLog
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-22 10:43
 * Modified by:
 */
@Data
public class InterfaceLog extends AbstractGrokField {
	
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
	private String hostname;
	
	@GrokAttributes(order = 7, regularKey = "", errMsg = "")
	private String port;
	
	@GrokAttributes(order = 8, regularKey = "", errMsg = "")
	private String sys_name;
	
	@GrokAttributes(order = 9, regularKey = "", errMsg = "")
	private String apply_name;
	
	@GrokAttributes(order = 10, regularKey = "", errMsg = "")
	private String client_ip;
	
	@GrokAttributes(order = 11, regularKey = "", errMsg = "")
	private String client_type;
	
	@GrokAttributes(order = 12, regularKey = "", errMsg = "")
	private String client_info;
	
	@GrokAttributes(order = 13, regularKey = "", errMsg = "")
	private String user_id;
	
	@GrokAttributes(order = 14, regularKey = "", errMsg = "")
	private String req_url;
	
	@GrokAttributes(order = 15, regularKey = "", errMsg = "")
	private String start_time;
	
	@GrokAttributes(order = 16, regularKey = "", errMsg = "")
	private String total_time;
	
	@GrokAttributes(order = 17, regularKey = "", errMsg = "")
	private String result_status;
	
	@GrokAttributes(order = 18, regularKey = "", errMsg = "")
	private String reqMap;
	
	@GrokAttributes(order = 19, regularKey = "", errMsg = "")
	private String req_message;
	
	@GrokAttributes(order = 20, regularKey = "", errMsg = "")
	private String res_message;

}
