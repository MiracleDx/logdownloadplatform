package com.log.download.platform.controller;

import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.vo.LogDetailVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * LogController
 * 日志查询控制器
 * @author Dongx
 * Description:
 * Created in: 2020-03-16 15:42
 * Modified by:
 */
@RestController
public class LogController {

	/**
	 * 查询对应部署组下的日志清单
	 * @return
	 */
	@GetMapping("/queryLogDetails")
	public ServerResponse<List<LogDetailVO>> queryLogDetails() {
		LogDetailVO logDetail = new LogDetailVO();
		logDetail.setId(1);
		logDetail.setPath("/default/logs/00008003-c008-00008003-0000-test-5bb4ff6f59-p2d4j-aoplog.log");
		return ServerResponse.success(Arrays.asList(logDetail, logDetail, logDetail));
	}
}
