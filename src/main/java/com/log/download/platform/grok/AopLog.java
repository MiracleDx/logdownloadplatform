package com.log.download.platform.grok;

import lombok.Data;

/**
 * AopLog
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-19 17:30
 * Modified by:
 */
@Data
public class AopLog extends AbstractGrokField {

	@GrokAttributes(order = 0, regularKey = "TIMESTAMP_ISO8601")
	private String timestamp;

	@GrokAttributes(order = 1, regularKey = "thread", errMsg = "线程名称不正确")
	private String threadName;

	@GrokAttributes(order = 2, regularKey = "level")
	private String logLevel;

	@GrokAttributes(order = 3, regularKey = "hostname")
	private String hostname;

	@GrokAttributes(order = 4, regularKey = "ipPort")
	private String ipPort;

	@GrokAttributes(order = 5, regularKey = "serverName")
	private String serverName;

	@GrokAttributes(order = 6, regularKey = "optUser")
	private String optUser;

	@GrokAttributes(order = 7, regularKey = "requestUrl")
	private String requestUrl;

	@GrokAttributes(order = 8, regularKey = "businessInfo")
	private String businessInfo;

	@GrokAttributes(order = 9, regularKey = "spendTime")
	private String spendTime;

	@GrokAttributes(order = 10, regularKey = "requestUrl")
	private String startTime;

	@GrokAttributes(order = 11, regularKey = "IPORHOST")
	private String clientIp;

	@GrokAttributes(order = 12, regularKey = "traceId")
	private String pGlobalTraceId;

	@GrokAttributes(order = 13, regularKey = "traceId")
	private String pParentTraceId;

	@GrokAttributes(order = 14, regularKey = "traceId")
	private String pLocalTraceId;

}
 