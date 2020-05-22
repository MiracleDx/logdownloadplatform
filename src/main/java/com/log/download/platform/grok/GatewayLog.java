package com.log.download.platform.grok;

import lombok.Data;

/**
 * GatewayLog
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-22 11:28
 * Modified by:
 */
@Data
public class GatewayLog extends AbstractGrokField {
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String func;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String req_uri;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String req_method;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String req_ip;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String req_token;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String apply_info;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String provide_info;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String msg;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String reqVo;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "")
	private String respVo;
	
	@GrokAttributes(order = 1, regularKey = "", errMsg = "") 
	private String error;
	
	
}
