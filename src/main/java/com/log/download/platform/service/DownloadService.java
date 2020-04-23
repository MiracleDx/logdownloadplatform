package com.log.download.platform.service;

import com.log.download.platform.context.RequestContext;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.util.FileUtil;
import com.log.download.platform.util.LogUtil;
import org.springframework.stereotype.Service;

/**
 * DownloadService
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-15 10:52
 * Modified by:
 */
@Service
public class DownloadService {

	/**
	 * 日志下载
	 * @param downLoadDTO
	 */
	public void download(DownLoadDTO downLoadDTO) {
		// todo 微服务网关下载路径变更
		String path = LogUtil.getInstance().processingCvmPath(downLoadDTO.getPath(), downLoadDTO.getHostname());
		// 下载
		FileUtil.getInstance().download(downLoadDTO.getIp(), path, RequestContext.getResponse());
	}
}
