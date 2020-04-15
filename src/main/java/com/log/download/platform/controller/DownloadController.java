package com.log.download.platform.controller;


import com.log.download.platform.context.StrategyFactory;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.exception.InternalServerException;
import com.log.download.platform.response.ResponseCode;
import com.log.download.platform.service.DownloadService;
import com.log.download.platform.service.IBaseService;
import com.log.download.platform.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * DownloadLogController
 * 日志下载控制器
 *
 * @author YaoDF
 * Description:
 * Created in: 2020-03-16 15:42
 * Modified by:
 */
@Slf4j
@RestController
public class DownloadController {

    @Resource
	private StrategyFactory strategyFactory;
    
    @Resource
    private DownloadService downloadService;

    /**
     * 普通微服务
     */
    private static final String SERVER = "server";

    /**
     * 微服务网关
     */
    private static final String gateway = "gateway";


    /**
     * 从本地获取镜像日志
     *
     * @param downLoadDTO
     */
    @RequestMapping("/downloadImage")
    public void downloadImage(@RequestBody DownLoadDTO downLoadDTO) {
        downloadService.download(downLoadDTO);
    }

	@RequestMapping("/download")
	public void download(@RequestBody DownLoadDTO downLoadDTO) {
        IBaseService service = null;
        LogUtil.LogEnum logType = LogUtil.getInstance().logType(downLoadDTO.getPath());
        if (logType == LogUtil.LogEnum.server) {
            service = strategyFactory.getStrategy(SERVER);
        } else if (logType == LogUtil.LogEnum.gateway){
            service = strategyFactory.getStrategy(gateway);
        } else {
            throw new InternalServerException(ResponseCode.SPECIFIED_QUESTIONED_USER_NOT_EXIST);
        }
        // 蓝鲸分发
        service.fastPushFile(downLoadDTO);
        downloadService.download(downLoadDTO);
	}
}
