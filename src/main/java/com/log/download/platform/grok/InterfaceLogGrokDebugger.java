package com.log.download.platform.grok;

import org.springframework.stereotype.Service;

/**
 * InterfaceLogGrokDebugger
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-22 10:48:21
 * Modified by:
 */
@Service
public class InterfaceLogGrokDebugger extends AbstractGrokDebugger {
	
	@Override
	public String logExample() {
		return "[2020-03-16 17:28:19,225] [g_OltAVdSDKMm5lVrHEWuw] [0] [g_OltAVdSDKMm5lVrHEWuw] [http-nio-19010-exec-5] [INFO] [DESKTOP-DV52DQ8/169.254.123.105] [http://169.254.123.105:19010]  [echo-server] [] [169.254.123.105:19010] [web] [Mozilla/5.0 (Windows NT 10.0; Win64; x64)] [zhangtian] [http://localhost:19010/demo/echo/echoStringByObject] [2020-03-16 17:28:19,222] [3] [200] [value=123] [{\"time\":1584313998000,\"value\":\"string\"}] [call echoStringByObject: string at 2020-03-16 07:13:18]";
	}
	
}
