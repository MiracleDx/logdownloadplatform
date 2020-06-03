package com.log.download.platform.service;

import com.log.download.platform.context.RequestContext;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.util.FileUtil;
import com.log.download.platform.util.IpUtil;
import com.log.download.platform.util.LogUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

	public void downloads(ArrayList<DownLoadDTO> list) throws Exception{
		String[] paths = new String[list.size()];
		InputStream[] ins = new FileInputStream[list.size()];
		for (int i = 0; i < list.size(); i++) {
			DownLoadDTO downLoadDTO = list.get(i);
			String path = LogUtil.getInstance().processingCvmPath(downLoadDTO.getPath(), downLoadDTO.getHostname());
			FileInputStream in = new FileInputStream(path);
			paths[i] = path;
			ins[i] = in;
		}
		String userIP = IpUtil.getRealIp(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
		String nowTime = new Long(System.currentTimeMillis()).toString();
		String zipPath = "/log/" + userIP + "/logs-" + nowTime +".zip";
		FileUtil.getInstance().zipFile(paths, zipPath , ins);
	}
}
