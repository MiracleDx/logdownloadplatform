package com.log.download.platform.controller;

import com.log.download.platform.context.RequestContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
public class IndexController {
	
	@RequestMapping
	public void hello() {
		try {
			HttpServletRequest request = RequestContext.getRequest();
			String serverName = request.getServerName();
			int port = request.getServerPort();
			// 不是管控区 && 访问的是8080端口  重定向到集群
			if (!serverName.contains("208") && port == 8080) {
				RequestContext.getResponse().sendRedirect("http://10.155.208.70:80");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
