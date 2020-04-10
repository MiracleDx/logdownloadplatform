package com.log.download.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.vo.LogDetailVO;

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
	 * 获取文件是否合理存在于文件服务器
	 *
	 * @param list
	 * @return
	 */
	default List<LogDetailVO> getFileIsExists(List<LogDetailVO> list) {
		List<LogDetailVO> logs = new ArrayList<>();
		String path;
		for (LogDetailVO log : list) {
			path = "/tmp" + File.separator + "0_" + log.getIp() + File.separator + processingContainerRealPath(log.getPath());
			File file = new File(path);
			if (!file.exists()) {
				log.setMirror(false);
			} else {
				//调用服务器指令，获取最后修改时间
				String mtime = executeLinuxCmd(path);
				if (isToday(log.getCreateTime(), mtime)) {
					log.setMirror(true);
				} else {
					file.delete();
					log.setMirror(false);
				}
			}
			logs.add(log);
		}
		return logs;
	}

	/**
	 * 判断文件在文件服务的时间是否合法
	 *
	 * @param creatTime 文件在蓝鲸的创建时间
	 * @param time      文件在文件服务器的最后修改时间
	 * @return
	 */
	default Boolean isToday(String creatTime, String time) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime localDateTime = LocalDateTime.parse(creatTime, dtf);
		LocalDateTime modifiedTime = LocalDateTime.parse(time, dtf);
		LocalDateTime now = LocalDateTime.now();
		//如果是蓝鲸创建时间是今天，判断文件在服务器上创建时间和当前时间是否间隔两个小时
		if (now.getYear() == localDateTime.getYear() && now.getMonth() == localDateTime.getMonth()
				&& now.getDayOfMonth() == localDateTime.getDayOfMonth()) {
			//获取两个时间的时间间隔
			Duration duration = Duration.between(modifiedTime, now);
			return duration.toMinutes() <= 120;
		} else {
			return true;
		}
	}

	/**
	 * 获取文件在服务器上的生成时间
	 * @param path
	 * @return
	 */
	default String executeLinuxCmd(String path) {
		String cmd = "stat " + path + " | grep Modify";
		Runtime run = Runtime.getRuntime();
		try {
			Process process = run.exec(new String[] {"/bin/sh", "-c", cmd});
			InputStream in = process.getInputStream();
			BufferedReader bs = new BufferedReader(new InputStreamReader(in));
			List<String> list = new ArrayList<String>();
			String result = null;
			while ((result = bs.readLine()) != null) {
				System.out.println("job result [" + result + "]");
				list.add(result);
			}
			in.close();
			process.destroy();

			return list.get(0).substring(0, list.get(0).lastIndexOf(":")).replace("Modify: ", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void test();
}
