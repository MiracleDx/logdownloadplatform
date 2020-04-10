package com.log.download.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.dto.FindMirrorDTO;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.vo.LogDetailVO;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * IBaseService
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-10 9:16
 * Modified by:
 */
public interface IBaseService {
	
	/**
	 * @param path
	 * @param response
	 */
	default void download(String ip, String path, HttpServletResponse response) throws IOException {
		path = "/tmp" + File.separator + "0_" + ip + File.separator + processingContainerRealPath(path);
		// path是指欲下载的文件的路径。
		File file = new File(path);
		if (!file.exists()) {
			response.setCharacterEncoding("utf-8");
			response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("文件" + path + "不存在")));
		}
		// 取得文件名。
		String filename = file.getName();
		// 取得文件的后缀名。
		String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();

		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(path));
			 OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());) {
			// 以流的形式下载文件。
			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);

			// 清空response
			response.reset();
			// 设置response的Header
			response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
			response.addHeader("Content-Length", "" + file.length());

			response.setContentType("application/octet-stream");
			outputStream.write(buffer);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 处理容器日志路径
	 * @param path
	 * tmp[1]=中心名称  c014
	 * tmp[2]=应用名称  01014020
	 * tmp[3]=分公司编码  3300
	 * tmp[4]=部署组id  1
	 * @return
	 */
	default String processingContainerRealPath(String path) {
		if (path.contains("/tsf_default/") && !path.contains("sys_log.log")){
			String[] temp = path.split("-");
			path = path.replace("/data/tsf_default/logs","/log/" + temp[1] + "-" + temp[2] + "-" + temp[3] + "-" + temp[4]);
		}
		return path;
	}


	/**
	 * 查询对应部署组下的日志清单
	 * @param queryLogDetailDTO
	 * @return
	 */
	List<LogDetailVO> queryLogDetails(QueryLogDetailDTO queryLogDetailDTO);

	/**
	 * 判断镜像文件是否存在
	 * @param findMirrorDTO
	 * @return
	 */
	Boolean findMirror(@RequestBody FindMirrorDTO findMirrorDTO);
}
