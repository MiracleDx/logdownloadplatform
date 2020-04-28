package com.log.download.platform.util;

import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.bo.JobStatusBO;
import com.log.download.platform.common.BkConstant;
import com.log.download.platform.common.BkEnum;
import com.log.download.platform.dto.HostDTO;
import com.log.download.platform.exception.NotImplementedException;
import com.log.download.platform.exception.RemoteAccessException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BkUtil
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-10 10:29
 * Modified by:
 */
public class BkUtil {

    private Logger logger = LoggerFactory.getLogger(BkUtil.class);

    /**
     * 应用ID
     */
    private final String BK_APP_CODE = "logdownder";

    /**
     * 安全秘钥
     */
    private final String BK_APP_SECRET = "855d69c9-2ed4-4d08-9a88-7a56a2564e12";

    /**
     * 当前用户用户名
     */
    private final String BK_USERNAME = "admin";

    /**
     * 共享服务器地址
     */
    private final String SHARED_SERVER = "10.155.27.48";

    /**
     * 调用蓝鲸执行脚本url
     */
    private final String FAST_EXECUTE_SCRIPT_URL = "http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/fast_execute_script/";

    /**
     * 调用蓝鲸查询脚本结果url
     */
    private final String GET_JOB_INSTANCE_LOG_URL = "http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/";

    /**
     * 调用蓝鲸快速分发url
     */
    private final String FAST_PUSH_FILE_URL = "http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/fast_push_file/";
    
    private final String IP = getLocalIp();

    private BkUtil() {

    }

    private static class SingletonInstance {
        private static final BkUtil INSTANCE = new BkUtil();
    }

    public static BkUtil getInstance() {
        return SingletonInstance.INSTANCE;
    }
    
    private ThreadLocal<AtomicInteger> retry_count = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    /**
     * 调用蓝鲸文件分发接口
     * @param params
     * @param restTemplate
     * @return
     */
    public JSONObject requestFastExecuteScript(String params, RestTemplate restTemplate) {
        return requestBkInterface(FAST_EXECUTE_SCRIPT_URL, params, restTemplate);
    }

    /**
     * 调用蓝鲸快速执行脚本
     * @param params
     * @param restTemplate
     * @return
     */
    public JSONObject requestFastPushFile(String params, RestTemplate restTemplate) {
        return requestBkInterface(FAST_PUSH_FILE_URL, params, restTemplate);
    }

    /**
     * 查询蓝鲸脚本执行情况
     * @param params
     * @param restTemplate
     * @return
     */
    public JSONObject requestGetJobInstanceLog(String params, RestTemplate restTemplate) {
        return requestBkInterface(GET_JOB_INSTANCE_LOG_URL, params, restTemplate);
    }

    /**
     * 获取蓝鲸执行脚本id
     * @param params
     * @param restTemplate
     * @return
     */
    public Integer getJobInstanceId(String params, RestTemplate restTemplate) {
        JSONObject jsonObject = requestFastExecuteScript(params, restTemplate);
        // 获取不到作业id的异常处理
        return getJobInstanceId(jsonObject);
    }

    /**
     * 获取蓝鲸执行脚本id
     * @param jsonObject
     * @return
     */
    public Integer getJobInstanceId(JSONObject jsonObject) {
        String data = jsonObject.getString(BkConstant.DATA);
        if (StringUtils.isEmpty(data) || !jsonObject.getString(BkConstant.DATA).contains(BkConstant.JOB_INSTANCE_ID)) {
            throw new NotImplementedException("蓝鲸接口返回错误：" + jsonObject.getString(BkConstant.MESSAGE));
        }
        return jsonObject.getJSONObject(BkConstant.DATA).getInteger(BkConstant.JOB_INSTANCE_ID);
    }

    /**
     * 获取蓝鲸执行脚本状态
     * @param jsonObject
     * @return
     */
    public Boolean getJobInstanceStatus(JSONObject jsonObject) {
        return jsonObject.getBoolean(BkConstant.RESULT);
    }

    /**
     * 调用蓝鲸接口
     *
     * @param url
     * @param params
     * @param restTemplate
     * @return
     */
    public JSONObject requestBkInterface(String url, String params, RestTemplate restTemplate) {
        //请求头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Accept-Encoding", "gzip");
        headers.add("Content-Encoding", "UTF-8");
        headers.add("Content-Type", "application/json; charset=UTF-8");
        HttpEntity<String> httpEntity = new HttpEntity<>(params, headers);
        //发送请求调用接口
        Instant startTime = Instant.now();
		ResponseEntity<String> request = null;
		try {
			request = restTemplate.postForEntity(url, httpEntity, String.class);
		} catch (RestClientException e) {
            // 重试次数+1
            retry_count.get().getAndIncrement();
            // 重试
            logger.info("bk interface begin request retry, retry count {} times", retry_count.get());
            // 大于重试次数 退出
            if (retry_count.get().intValue() >= 3) {
                logger.error("bk interface read timed out, request url: {}, request params: {}", url, params);
                retry_count.remove();
                throw new RemoteAccessException("蓝鲸接口返回错误：Read timed out 请稍后重试");
            }
            return requestBkInterface(url, params, restTemplate);
		}
        Instant endTime = Instant.now();
        logger.info("request url: {}, request params: {}, request response: {}, spendTime: {}ms", url, params, request.getBody(), Duration.between(startTime, endTime).toMillis());
        retry_count.remove();
        return JSONObject.parseObject(request.getBody());
    }

    /**
     * 获取蓝鲸脚本执行状态及脚本结果（超时中断任务）
     * @param bkBizId
     * @param jobInstanceId
     * @param restTemplate
     * @return
     */
    public JobStatusBO getJobStatus(int bkBizId, int jobInstanceId, RestTemplate restTemplate) {
        int count = 0;
        // 获取蓝鲸脚本查询参数
        String params = getJobInstanceLogParams(bkBizId, jobInstanceId);
        long t1 = System.currentTimeMillis();
        boolean isFinished = false;
        //循环调用查询作业执行情况 到达等待时间的阈值，会直接中断
        JSONObject resultLog = null;
        count = 0;
        while (true) {
            resultLog = requestGetJobInstanceLog(params, restTemplate);
            isFinished = resultLog.getJSONArray(BkConstant.DATA).getJSONObject(0).getBoolean(BkConstant.IS_FINISHED);
            
            // 执行结束直接跳出循环
            if (isFinished) {
                break;
            }

            logger.info("request fast_execute_script {} times", count);
            count ++;
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long t2 = System.currentTimeMillis();
            if (t2 - t1 > 60 * 1000) {
                break;
            }
        }

        JobStatusBO jobStatusBO = new JobStatusBO();
        jobStatusBO.setIsFinished(isFinished);
        jobStatusBO.setResult(resultLog);
        return jobStatusBO;
    }

    /**
     * 获取快速执行脚本入参
     *
     * @param bkBizId
     * @param ips
     * @param param
     * @param scriptId
     * @return
     */
    public String getFastExecuteScriptParams(int bkBizId, String[] ips, String param, int scriptId) {
        byte[] content = param.getBytes();
        String scriptParam = Base64.getEncoder().encodeToString(content);

        String params = "{\n" +
                "\t\"bk_app_code\": \"" + BK_APP_CODE + "\",\n" +
                "\t\"bk_app_secret\": \"" + BK_APP_SECRET + "\",\n" +
                "\t\"bk_username\": \"" + BK_USERNAME + "\",\n" +
                "\t\"bk_biz_id\": " + bkBizId + ",\n" +
                "\t\"script_id\": " + scriptId + ",\n" +
                "\t\"script_param\": \"" + scriptParam + "\",\n" +
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

        return resultParam.toJSONString();
    }

    /**
     * 获取脚本执行结果入参
     *
     * @param bkBizId
     * @param jobInstanceId
     * @return
     */
    public String getJobInstanceLogParams(int bkBizId, int jobInstanceId) {
        String params = "{\n" +
                "\t\"bk_app_code\": \"" + BK_APP_CODE + "\",\n" +
                "\t\"bk_app_secret\": \"" + BK_APP_SECRET + "\",\n" +
                "\t\"bk_username\": \"" + BK_USERNAME + "\",\n" +
                "\t\"bk_biz_id\": " + bkBizId + ",\n" +
                "\t\"job_instance_id\": " + jobInstanceId + "\n" +
                "}";
        return params;

    }

    /**
     * 获取蓝鲸业务Id
     * @param label
     * @return
     */
    public int getBkBizId(String label) {
        return BkEnum.valueOf(label.toUpperCase()).getCode();
    }

    /**
     * 拼接快速分发文件的接口参数
     *
     * @param bkBizId
     * @param ip
     * @param path
     * @param cvmIp
     * @return
     */
    public String getFastPushFileParams(int bkBizId, String ip, String path, String cvmIp) {
        int end = path.lastIndexOf("/");
        String params = "{\n" +
                "\t\"bk_app_code\": \"" + BK_APP_CODE + "\",\n" +
                "\t\"bk_app_secret\": \"" + BK_APP_SECRET + "\",\n" +
                "\t\"bk_username\": \"" + BK_USERNAME + "\",\n" +
                "\t\"bk_biz_id\": " + bkBizId + ",\n" +
                "\t\"file_target_path\": \"/tmp/0_" + cvmIp + "/" + path.substring(0, end) + "\",\n" +
                "\t\"account\": \"root\",\n" +
                "\t\"ip_list\": [{\"bk_cloud_id\": 0,\"ip\": \"" + IP + "\"}],\n" +
                "\t\"file_source\": [{\n" +
                "\t\t\"files\":[\"" + path + "\"],\n" +
                "\t\t\"account\": \"ubuntu\",\n" +
                "\t\t\"ip_list\": [\n";
        String ipList = "\t\t{\n" +
                "\t\t\t\"bk_cloud_id\": 0,\n" +
                "\t\t\t\"ip\": \"" + ip + "\"\n" +
                "\t\t}\n";

        params += ipList;
        params += "\t\t]\n\t}\n]}";
        return params;
    }

    /**
     * 获取/查询微服务容器入参
     *
     * @param bkBizId
     * @param ip
     * @param path
     * @param scriptId
     * @return
     */
    public String getServerContainerScriptParams(int bkBizId, String ip, String path, int scriptId, String hostname, String bkParam) {
        String[] arr = path.split("/");
        String logName = arr[arr.length - 1];
        String param = "";
        param = bkParam + " " + hostname + " " + logName;
        byte[] content = param.getBytes();
        String script_param = Base64.getEncoder().encodeToString(content);
        return getContainerScriptParams(bkBizId, ip, script_param, scriptId);
    }

    /**
     * 获取/查询微服务网关容器入参
     *
     * @param bkBizId
     * @param ip
     * @param path
     * @param scriptId
     * @return
     */
    //todo 微服务网关会出现同ip下多个微服务网关日志，还需要增加条件，路径中增加部署组
    public String getGetewayContainerScriptParams(int bkBizId, String ip, String path, String flag, int scriptId) {
        String[] arr = path.split("/");
        String logName = arr[arr.length - 1];
        String[] paths = flag.split("-");
        String namespace = paths[0] + "" + paths[1] + "-" + paths[2] + "-" + paths[3];
        String group = namespace;
        String param = namespace + " " + group + " " + flag + " " + logName;
        byte[] content = param.getBytes();
        String script_param = Base64.getEncoder().encodeToString(content);
        return getContainerScriptParams(bkBizId, ip, script_param, scriptId);
    }

    public String getContainerScriptParams(int bkBizId, String ip, String script_param, int scriptId) {
        String params = "{\n" +
                "\t\"bk_app_code\": \"" + BK_APP_CODE + "\",\n" +
                "\t\"bk_app_secret\": \"" + BK_APP_SECRET + "\",\n" +
                "\t\"bk_username\": \"" + BK_USERNAME + "\",\n" +
                "\t\"bk_biz_id\": " + bkBizId + ",\n" +
                "\t\"script_id\": " + scriptId + ",\n" +
                "\t\"script_param\": \"" + script_param + "\",\n" +
                "\t\"script_timeout\": 1000,\n" +
                "\t\"account\": \"ubuntu\",\n" +
                "\t\"is_param_sensitive\": 0,\n" +
                "\t\"ip_list\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"bk_cloud_id\":0,\n" +
                "\t\t\t\"ip\":\"" + ip + "\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        return params;
    }

    /**
     * 获取本机IP，获取不到的话返回共享服务器
     * @return
     */
    private String getLocalIp() {
        InetAddress ia = null;
        try {
            ia = InetAddress.getLocalHost();
            String localName = ia.getHostName();
            String localIp = IpUtil.getServiceIp();
            logger.info("hostname: {}, hostAddress: {}", localName, IpUtil.getServiceIp());
            return localIp;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return SHARED_SERVER;
    }

    public String getScriptParam() {
        String scriptParam = "";
        return scriptParam;
    }
}
