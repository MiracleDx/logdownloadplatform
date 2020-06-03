package com.log.download.platform.util;

import cn.hutool.core.util.ZipUtil;
import com.log.download.platform.exception.DataNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-04-13 09:37
 **/
@Slf4j
public class FileUtil {

    //120分钟为两小时
    public static final int TWOHOURS = 120;

    private FileUtil() {

    }

    private static class SingletonInstance {
        private static final FileUtil instance = new FileUtil();
    }

    public static FileUtil getInstance() {
        return SingletonInstance.instance;
    }

    /**
     * 下载文件
     *
     * @param path     文件在文件服务器上的路径
     * @param response
     */
    public void download(String ip, String path, HttpServletResponse response) {
        // path是指欲下载的文件的路径。
        path = "/tmp/0_" + ip + "/" + path;
        System.out.println(path);
        File file = new File(path);
        if (!file.exists()) {
            throw new DataNotFoundException("日志文件" + path.substring(path.lastIndexOf("/")) + "不存在");
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
            log.error(ex.getMessage());
        }
    }

    /**
     * 获取文件是否合理存在于文件服务器
     *
     * @param path
     * @param createTime
     * @return
     */
    public Boolean getFileIsExists(String path, String createTime) {
		File file = new File(path);
		if (!file.exists()) {
			return false;
		} else {
			//最后修改时间
			String mtime = executeLinuxCmd(path);
			if (isToday(createTime, mtime)) {
				return true;
			} else {
				file.delete();
				return false;
			}
		}
    }

    /**
     * 获取文件在服务器上的生成时间
     *
     * @param path 文件在文件服务器上的生成时间
     * @return
     */
    public String executeLinuxCmd(String path) {
        String cmd = "stat " + path + " | grep Modify";
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec(new String[]{"/bin/sh", "-c", cmd});
            InputStream in = process.getInputStream();
            BufferedReader bs = new BufferedReader(new InputStreamReader(in));
            List<String> list = new ArrayList<String>();
            String result = null;
            while ((result = bs.readLine()) != null) {
                list.add(result);
            }
            in.close();
            process.destroy();

            return list.get(0).substring(0, list.get(0).lastIndexOf(":")).replace("Modify: ", "");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 判断文件在文件服务的时间是否合法
     *
     * @param creatTime 文件在蓝鲸的创建时间
     * @param time      文件在文件服务器的最后修改时间
     * @return
     */
    public Boolean isToday(String creatTime, String time) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(creatTime, dtf);
        LocalDateTime modifiedTime = LocalDateTime.parse(time, dtf);
        LocalDateTime now = LocalDateTime.now();
        //如果是蓝鲸创建时间是今天，判断文件在服务器上创建时间和当前时间是否间隔两个小时
        if (now.getYear() == localDateTime.getYear() && now.getMonth() == localDateTime.getMonth()
                && now.getDayOfMonth() == localDateTime.getDayOfMonth()) {
            //获取两个时间的时间间隔，单位为分钟
            Duration duration = Duration.between(modifiedTime, now);
            //若时间间隔小于2小时，则说明合法存在
            return duration.toMinutes() <= TWOHOURS;
        }
        return true;
    }

    /**
     * 判断路径和文件名是否合法
     *
     * @param logPath
     * @return
     */
    public boolean pathLegal(String logPath) {
        if (!StringUtils.isEmpty(logPath) && !logPath.contains("---")
                && !logPath.contains("error: ")
                && !logPath.contains("No resources found")
                && !logPath.contains("No such file")
                && !logPath.contains("Unable to connect to the server")
                && !logPath.contains("Error from server")
                && !logPath.contains("cannot exec into a container")
                && !logPath.contains("command terminated with exit code 1")
                && !logPath.contains("certificate is valid")) {
            //日志文件名
            //String fileName = logPath.substring(logPath.lastIndexOf("/") + 1, logPath.length());
            // 日志路径
            String logName = logPath.substring(logPath.lastIndexOf("/"));
            if (!logName.contains("torrent")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static void main(String[] args) throws FileNotFoundException {
        FileUtil fileUtil = FileUtil.getInstance();
        String zipPath = "D:\\log\\ccc.zip";
        String[] paths = new String[3];
        paths[0] = "D:\\log\\aaa.txt";
        paths[1] = "D:\\log\\bbb.txt";
        paths[2] = "D:\\log\\ccc.txt";
        InputStream[] ins = new FileInputStream[3];
        ins[0] = new FileInputStream(paths[0]);
        ins[1] = new FileInputStream(paths[1]);
        ins[2] = new FileInputStream(paths[2]);
        fileUtil.zipFile(paths, zipPath, ins);
    }

    public void zipFile(String[] path, String zipPath, InputStream[] streams) {
        ZipUtil.zip(new File(zipPath), path, streams);
    }

    public void unZipFile(String path, String zipPath) {

    }




}
