package com.log.download.platform.grok;

import org.springframework.stereotype.Service;

/**
 * BusinessLogGrokDebugger
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-22 10:41:32
 * Modified by:
 */
@Service
public class BusinessLogGrokDebugger extends AbstractGrokDebugger {
	
	@Override
	public String logExample() {
		return "[2020-03-16 17:28:19,224] [g_OltAVdSDKMm5lVrHEWuw] [0] [g_OltAVdSDKMm5lVrHEWuw] [http-nio-19010-exec-5] [INFO] [DESKTOP-DV52DQ8/169.254.123.105] [169.254.123.105:19010] [echo-server] [2020-03-16 17:28:19,224]  [pdfc.platform.demo.service.EchoService.echoByObject] [{\"time\":1584313998000,\"value\":\"string\"}] [call echoStringByObject: string at 2020-03-16 07:13:18] [0]  [自定义业务日志]";
	}
	
}
