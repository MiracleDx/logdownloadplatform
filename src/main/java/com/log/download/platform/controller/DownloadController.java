package com.log.download.platform.controller;


import com.log.download.platform.aop.OperatingLog;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.service.DownloadService;
import com.log.download.platform.service.IBaseService;
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
	private IBaseService iBaseService;
    
    @Resource
    private DownloadService downloadService;

    /**
     * 从本地获取镜像日志
     *
     * @param downLoadDTO
     */
    @OperatingLog("downloadImage")
    @RequestMapping("/downloadImage")
    public void downloadImage(@RequestBody DownLoadDTO downLoadDTO) {
        downloadService.download(downLoadDTO);
    }

    @OperatingLog("download")
	@RequestMapping("/download")
	public void download(@RequestBody DownLoadDTO downLoadDTO) {
        // 蓝鲸分发
		iBaseService.fastPushFile(downLoadDTO);
        downloadService.download(downLoadDTO);
	}
}
