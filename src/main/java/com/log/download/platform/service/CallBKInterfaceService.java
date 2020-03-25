package com.log.download.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.common.BkEnum;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.dto.HostDTO;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.response.ServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * CallBKInterfaceService
 * 蓝鲸接口调用
 *
 * @author YaoDF
 * Description:
 * Created in: 2020-03-16 15:42
 * Modified by:
 */
@Slf4j
@Service
public class CallBKInterfaceService {


    @Value("${bk_app_code}")
    private String bk_app_code;

    @Value("${bk_app_secret}")
    private String bk_app_secret;

    @Value("${bk_username}")
    private String bk_username;

    @Value("${getlogpath_script_id}")
    private int script_id;

    /**
     * base64编码
     */
    final Base64.Encoder encoder = Base64.getEncoder();


    /**
     * 根据传参和url调用蓝鲸接口
     *
     * @param url
     * @param params
     * @return
     */
    public JSONObject callLanJingInterface(String url, String params) {
        RestTemplate restTemplate = new RestTemplate();
        //请求头
        HttpHeaders headers = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(params, headers);
        //发送请求调用接口
        ResponseEntity<String> request = restTemplate.postForEntity(url, httpEntity, String.class);
        JSONObject resultData = JSONObject.parseObject(request.getBody());
        return resultData;
    }

    /**
     * 获取快速执行脚本入参
     *
     * @param queryLogDetailDTO
     * @return
     */
    public String getFastExecuteScriptParams(QueryLogDetailDTO queryLogDetailDTO) {
        String label = queryLogDetailDTO.getLabel();
        String[] ips = queryLogDetailDTO.getIps();
        String param = queryLogDetailDTO.getBkParam();
        BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
        int bk_biz_id = bkEnum.getCode();
        byte[] content = param.getBytes();
        String script_param = encoder.encodeToString(content);
        int fastExecuteScript_id = script_id;
        if (queryLogDetailDTO.getIsHistory()){
            fastExecuteScript_id = 0;
        }
        String params = "{\n" +
                "\t\"bk_app_code\": \"" + bk_app_code + "\",\n" +
                "\t\"bk_app_secret\": \"" + bk_app_secret + "\",\n" +
                "\t\"bk_username\": \"" + bk_username + "\",\n" +
                "\t\"bk_biz_id\": " + bk_biz_id + ",\n" +
                "\t\"script_id\": " + fastExecuteScript_id + ",\n" +
                "\t\"script_param\": \"" + script_param + "\",\n" +
                "\t\"script_timeout\": 1000,\n" +
                "\t\"account\": \"ubuntu\",\n" +
                "\t\"is_param_sensitive\": 0,\n" +
                "}";
        List<Map<String, Object>> list = new ArrayList<>();
        HostDTO hostDTO = new HostDTO();
        for (String ip : ips) {
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

    public String getJobInstanceLogParams(String label, int job_instance_id) {
        BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
        int bk_biz_id = bkEnum.getCode();
        String params = "{\n" +
                "\t\"bk_app_code\": \"" + bk_app_code + "\",\n" +
                "\t\"bk_app_secret\": \"" + bk_app_secret + "\",\n" +
                "\t\"bk_username\": \"" + bk_username + "\",\n" +
                "\t\"bk_biz_id\": " + bk_biz_id + ",\n" +
                "\t\"job_instance_id\": " + job_instance_id + "\n" +
                "}";
        return params;

    }

    public String getFastPushFile(DownLoadDTO downLoadDTO) {
        String label = downLoadDTO.getLabel();
        BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
        int bk_biz_id = bkEnum.getCode();
        String ip = downLoadDTO.getIp();
        String path = downLoadDTO.getPath();
        int end = path.lastIndexOf("/");
        String params = "{\n" +
                "\t\"bk_app_code\": \"" + bk_app_code + "\",\n" +
                "\t\"bk_app_secret\": \"" + bk_app_secret + "\",\n" +
                "\t\"bk_username\": \"" + bk_username + "\",\n" +
                "\t\"bk_biz_id\": " + bk_biz_id + ",\n" +
                "\t\"file_target_path\": \"/tmp/0_" + ip + "/" + path.substring(0, end) + "\",\n" +
                "\t\"account\": \"root\",\n" +
                "\t\"ip_list\": [{\"bk_cloud_id\": 0,\"ip\": \"10.155.27.48\"}],\n" +
                "\t\"file_source\": [{\n" +
                "\t\t\"files\":[\"" + path + "\"],\n" +
                "\t\t\"account\": \"ubuntu\",\n" +
                "\t\t\"ip_list\": [\n";
        String iplist = "\t\t{\n" +
                        "\t\t\t\"bk_cloud_id\": 0,\n" +
                        "\t\t\t\"ip\": \"" + ip + "\"\n" +
                        "\t\t}\n";

        params += iplist;
        params += "\t\t]\n\t}\n]}";
        return params;
    }

    /**
     *
     * @param path
     * @param response
     */
    public void download(String ip, String path, HttpServletResponse response) throws IOException {

        path = "/tmp" + File.separator + "0_" + ip + File.separator + path;
        // path是指欲下载的文件的路径。
        File file = new File(path);
        if (!file.exists()) {
            log.error("文件不存在");
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
}
