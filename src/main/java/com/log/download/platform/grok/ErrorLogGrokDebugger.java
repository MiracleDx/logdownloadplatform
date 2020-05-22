package com.log.download.platform.grok;

import org.springframework.stereotype.Service;

/**
 * ErrorLogGrokDebugger
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-22 11:27:38
 * Modified by:
 */
@Service
public class ErrorLogGrokDebugger extends AbstractGrokDebugger {
	
	@Override
	public String logExample() {
		return "[2020-03-16 17:28:19,225] [http-nio-19010-exec-5] [INFO] [DESKTOP-DV52DQ8/169.254.123.105] [http://169.254.123.105:19010] [echo-server] [pdfc.platform.demo.service.EchoServiceechoObjectByObject] [堆栈信息] [错误处理信息] [g_OltAVdSDKMm5lVrHEWuw] [0] [g_OltAVdSDKMm5lVrHEWuw] ";
	}
	
}
