package com.log.download.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.common.BkEnum;
import com.log.download.platform.dto.HostDTO;
import com.log.download.platform.dto.QueryLogDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class CallBKInterfaceService {


    private static String bk_app_code = "logdownder";
    private static String bk_app_secret = "855d69c9-2ed4-4d08-9a88-7a56a2564e12";
    private static String bk_token = "zWcmCWfovsudD1N9EQ_gzl6Z5ZNIlG09vFH8c8JGT_s";
    private static int script_id = 8403;
    //base64编码
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


        String params = "{\n" +
                "\t\"bk_app_code\": \"" + bk_app_code + "\",\n" +
                "\t\"bk_app_secret\": \"" + bk_app_secret + "\",\n" +
                "\t\"bk_token\": \"" + bk_token + "\",\n" +
                "\t\"bk_biz_id\": " + bk_biz_id + ",\n" +
                "\t\"script_id\": " + script_id + ",\n" +
                "\t\"script_param\": \"" + script_param + "\",\n" +
                "\t\"script_timeout\": 1000,\n" +
                "\t\"account\": \"root\",\n" +
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
                "\t\"bk_token\": \"" + bk_token + "\",\n" +
                "\t\"bk_biz_id\": " + bk_biz_id + ",\n" +
                "\t\"job_instance_id\": " + job_instance_id + ",\n" +
                "}";
        return params;

    }

    public static void main(String[] args) {
        QueryLogDetailDTO queryLogDetailDTO = new QueryLogDetailDTO();
        queryLogDetailDTO.setLabel("c014");
        queryLogDetailDTO.setBkParam("c014-3300 c014-01014020-3300-1");
        String[] ip = {"10.2.3.1","10.11.2.3"};
        queryLogDetailDTO.setIps(ip);
        CallBKInterfaceService bk = new CallBKInterfaceService();
        String test = bk.getFastExecuteScriptParams(queryLogDetailDTO);
        System.out.println(test);
    }

}
