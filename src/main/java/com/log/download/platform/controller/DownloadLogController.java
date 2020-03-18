package com.log.download.platform.controller;


import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.common.BkEnum;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.dto.HostDTO;
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

    private BkEnum bkEnum;


    public void downloadLog(DownLoadDTO downLoadDTO){
        String label = downLoadDTO.getLabel();
        String[] ips = downLoadDTO.getIps();
        HashMap<String, Object> params = new HashMap<>();
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


    /**
     * 执行脚本
     * @param
     */
    @PostMapping("/executeScript")
    @ResponseBody
    public Boolean executeScript(String label, String[] ips, int script_id) {
        RestTemplate restTemplate = new RestTemplate();
        //远端接口设置
        String url = "https://bkpaas.piccit.com.cn/api/c/compapi/v2/job/fast_execute_script/";
        //配置参数
        String params = getFastExecuteScriptParams(label, ips, script_id);
        JSONObject resultData = callLanJingInterface(url,params);
        Boolean result = Boolean.valueOf(resultData.getJSONObject("result").toString());
        return result;
    }


    //调用蓝鲸接口
    public JSONObject callLanJingInterface(String url,String params){
        RestTemplate restTemplate = new RestTemplate();
        //请求头
        HttpHeaders headers = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(params,headers);
        //发送请求调用接口
        ResponseEntity<String> request = restTemplate.postForEntity(url,httpEntity, String.class);
        JSONObject resultData = JSONObject.parseObject(request.getBody());
        return resultData;
    }

    //蓝鲸查出日志接口
    public JSONObject getInstanceLog(String url,String params) {
        JSONObject json = callLanJingInterface(url,params);
        Boolean result = Boolean.valueOf(json.getJSONObject("result").toString());
        String log_content = json.getJSONArray("data").getJSONObject(0).getJSONArray("step_results").getJSONObject(0).getJSONArray("ip_logs").getJSONObject(0).getJSONObject("log_content").toString();
        return json;
    }

    /**
     * 获取快速执行脚本入参
     * @param label
     * @param ips
     * @return
     */
    public String getFastExecuteScriptParams(String label, String[] ips, int script_id){
        BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
        int bk_biz_id = bkEnum.getCode();
        String script_param = "YzAxNC0zMzAwIGMwMTQtMDEwMTQwMjAtMzMwMC0x";

        String params = "{\n" +
                "\t\"bk_app_code\": \""+bk_app_code+"\",\n" +
                "\t\"bk_app_secret\": \""+bk_app_secret+"\",\n" +
                "\t\"bk_token\": \""+bk_token+"\",\n" +
                "\t\"bk_biz_id\": "+ bk_biz_id +",\n" +
                "\t\"script_id\": "+ script_id +",\n" +
                "\t\"script_param\": \""+script_param+"\",\n" +
                "\t\"script_timeout\": 1000,\n" +
                "\t\"account\": \"root\",\n" +
                "\t\"is_param_sensitive\": 0,\n" +
                "}";
        List<Map<String,Object>> list = new ArrayList<>();
        HostDTO hostDTO = new HostDTO();
        for (String ip : ips){
            Map<String, Object> hostMap = new HashMap<>();
            hostMap.put("bk_cloud_id", 0);
            hostMap.put("ip", ip);
            list.add(hostMap);
            hostDTO.setIp_list(list);
        }
        String ipListJson = JSONObject.toJSON(hostDTO).toString();
        // 合并两段JSON
        JSONObject json = JSONObject.parseObject(params);
        JSONObject ipJson = JSONObject.parseObject(ipListJson);
        JSONObject resultParam = new JSONObject();
        resultParam.putAll(json);
        resultParam.putAll(ipJson);
        String paramResult = resultParam.toJSONString();

        return paramResult;
    }










}
