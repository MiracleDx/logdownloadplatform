package com.log.download.platform.controller;

import com.log.download.platform.context.RequestContext;
import com.log.download.platform.grok.AopLog;
import com.log.download.platform.grok.AopLogGrokDebugger;
import com.log.download.platform.grok.GrokResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * IndexController
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-07 15:51
 * Modified by:
 */
@Log4j2
@RestController
public class IndexController {
	
	@Resource
	private AopLogGrokDebugger aopLogGrokDebugger;
	
	@RequestMapping("/")
	public void hello() {
		try {
			String content = "[2020-01-09 11:25:07,630] [http-nio-19010-exec-2] [INFO] [LAPTOP/10.1.71.XX] [http://10.1.71.1XX:19010] [0201050201] [] [http://localhost:19010/importHealthQuickCaseClaim] [] [35845] [2020-01-09 11:24:31,784] [0:0:0:0:0:0:0:1] [740BM9tQLSgqLZazBNOUA] [0] [740BM9tQLSgqLZazBNOUA]";
			GrokResult grok = aopLogGrokDebugger.grok(content, AopLog.class);
			System.out.println(grok);
			HttpServletRequest request = RequestContext.getRequest();
			String serverName = request.getServerName();
			int port = request.getServerPort();
			// 不是管控区 && 访问的是8080端口  重定向到集群
			if (!serverName.contains("208") && port == 8080) {
				RequestContext.getResponse().sendRedirect("http://10.155.208.70:80");
			}
		} catch (IOException e) {
			log.error("exception occurred: {}", () -> e);
		}
	}
}
