package com.log.download.platform.controller;


import com.log.download.platform.dto.QueryLogDetailDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;


@RestController
public class DownloadLogController {


    public void downloadLog(@RequestBody QueryLogDetailDTO queryLogDetailDTO) {
        String label = queryLogDetailDTO.getLabel();
        String[] ips = queryLogDetailDTO.getIps();
    }

    @RequestMapping("download")
    public void download(String path, HttpServletResponse response) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            path = "d:/test.log";
            // path是指欲下载的文件的路径。
            File file = new File(path);
            // 取得文件名。
            String filename = file.getName();
            // 取得文件的后缀名。
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();

            // 以流的形式下载文件。
            inputStream = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);

            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
            response.addHeader("Content-Length", "" + file.length());
            outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            outputStream.write(buffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
