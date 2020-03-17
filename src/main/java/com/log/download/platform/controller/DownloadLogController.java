package com.log.download.platform.controller;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.springframework.util.StreamUtils.BUFFER_SIZE;


@RestController
public class DownloadLogController {


    /**
     * 公共参数
     */
    private static String APPCODE = "bksaas";
    private static String APPSECRET = "470c6438-1037-4a9e-9016-e520eaa7ae99";
    private static String TOKEN = "V5AFnjAkKvRuOvineSYiaVni3HmFTmCOvDrbjE4cIv0";
    private static String zipPath = "";

    @RequestMapping("download")
    public void download(String path, HttpServletResponse response) {
        try {
            path = "d:/test.log";
            // path是指欲下载的文件的路径。
            File file = new File(path);
            // 取得文件名。
            String filename = file.getName();
            // 取得文件的后缀名。
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();

            // 以流的形式下载文件。
            InputStream fis = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 执行脚本
     * @param
     */
    @PostMapping("/executeScript")
    @ResponseBody
    public static String executeScript() {
        RestTemplate restTemplate = new RestTemplate();
        //远端接口设置
        String url = "https://bkpaas.piccit.com.cn/api/c/compapi/v2/job/fast_execute_script/";
        //请求头
        HttpHeaders headers = new HttpHeaders();
        //配置参数
        Map<String, Object> params = new HashMap<>();
        params.put("bk_app_code", "");
        params.put("bk_app_secret", "");
        params.put("bk_token", "");
        params.put("ip_list", new ArrayList<>());
        HttpEntity httpEntity = new HttpEntity(params,headers);
        //发送请求调用接口
        ResponseEntity<String> request = restTemplate.postForEntity(url,httpEntity, String.class);
        JSONObject resultData = JSONObject.parseObject(request.getBody());
        Boolean result = Boolean.valueOf(resultData.getJSONObject("").toString());
        return "";
    }


    //调用蓝鲸接口
    public String callLanJingInterface(String url,HashMap<String, Object> params){
        RestTemplate restTemplate = new RestTemplate();
        //请求头
        HttpHeaders headers = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(params,headers);
        //发送请求调用接口
        ResponseEntity<String> request = restTemplate.postForEntity(url,httpEntity, String.class);
        JSONObject resultData = JSONObject.parseObject(request.getBody());
        Boolean result = Boolean.valueOf(resultData.getJSONObject("").toString());
        if (result) {
            return "success";
        } else {
            return "false";
        }
    }

    public File downloadExcel (HttpServletRequest request, HttpServletResponse response) throws IOException {
        //提供下载文件前进行压缩，即服务端生成压缩文件
        String path = "";
        File file = new File(zipPath);
        FileOutputStream fos = new FileOutputStream(file);
        toZip(path, fos, true);
        //1.获取要下载的文件的绝对路径
        String realPath = zipPath;
        //2.获取要下载的文件名
        String fileName = realPath.substring(realPath.lastIndexOf(File.separator)+1);
        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/octet-stream");
        //3.设置content-disposition响应头控制浏览器以下载的形式打开文件
        response.addHeader("Content-Disposition","attachment;filename=" + new String(fileName.getBytes(),"utf-8"));
        //获取文件输入流
        InputStream in = new FileInputStream(realPath);
        int len = 0;
        byte[] buffer = new byte[1024];
        OutputStream out = response.getOutputStream();
        while ((len = in.read(buffer)) > 0) {
            //将缓冲区的数据输出到客户端浏览器
            out.write(buffer,0,len);
        } in.close(); return file;

    }

    //压缩方法
    public static void toZip(String srcDir, OutputStream out, boolean KeepDirStructure) throws RuntimeException{
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null ;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile,zos,sourceFile.getName(),KeepDirStructure);
            long end = System.currentTimeMillis();
            System.out.println("压缩完成，耗时：" + (end - start) +" ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils",e);
        }finally{
            if(zos != null){
                try {
                zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean KeepDirStructure) throws Exception{
        byte[] buf = new byte[BUFFER_SIZE];
        if(sourceFile.isFile()){
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1){
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (KeepDirStructure) {
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠, // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(), KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), KeepDirStructure);
                    }

                }
            }
        }

    }



}
