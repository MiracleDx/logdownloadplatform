package com.log.download.platform.controller;


import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.common.BkEnum;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.dto.HostDTO;
import com.log.download.platform.dto.QueryLogDetailDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;


@RestController
public class DownloadLogController {


    /**
     * 公共参数
     */
    private static String APPCODE = "bksaas";
    private static String APPSECRET = "470c6438-1037-4a9e-9016-e520eaa7ae99";
    private static String TOKEN = "V5AFnjAkKvRuOvineSYiaVni3HmFTmCOvDrbjE4cIv0";

    private static String bk_app_code = "logdownder";
    private static String bk_app_secret = "855d69c9-2ed4-4d08-9a88-7a56a2564e12";
    private static String bk_token = "zWcmCWfovsudD1N9EQ_gzl6Z5ZNIlG09vFH8c8JGT_s";
    private static String bk_username = "";
    private static int script_id_1 = 8302;
    private static int script_id_2 = 0;
    //base64编码
    final Base64.Encoder encoder = Base64.getEncoder();



    public void downloadLog(@RequestBody QueryLogDetailDTO queryLogDetailDTO){
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
