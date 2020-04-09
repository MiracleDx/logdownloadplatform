package com.log.download.platform.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.log.download.platform.common.JsonWordEnum;
import com.log.download.platform.common.StatusEnum;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.service.CallBKInterfaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 * DownloadLogController
 * 日志下载控制器
 *
 * @author YaoDF
 * Description:
 * Created in: 2020-03-16 15:42
 * Modified by:
 */
@Slf4j
@RestController
public class DownloadLogController {

    /**
     * 调用接口返回的执行结果
     */
    public static final String RESULT = "result";
    @Resource
    private CallBKInterfaceService callBKInterfaceService;

    @Value("${fastExecuteScriptUrl}")
    private String fastExecuteScriptUrl;

    @Value("${fastPushFileUrl}")
    private String fastPushFileUrl;

    @Value("${getcontainerlog}")
    private int getcontainerlog;

    @Value("${getcontainerip}")
    private int getcontainerip;

    @Value("${REGEX_IP_ADDR}")
    private String REGEX_IP_ADDR;


    /**
     * 从本地获取镜像日志
     *
     * @param downLoadDTO
     * @throws IOException
     */
    @RequestMapping("/downloadImage")
    public void downloadImage(@RequestBody DownLoadDTO downLoadDTO) throws IOException {
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        callBKInterfaceService.download(downLoadDTO.getIp(), downLoadDTO.getPath(), response);
    }

    /**
     * 从蓝鲸获取日志
     *
     * @param downLoadDTO
     * @throws IOException
     */
    @RequestMapping("/download")
    public void downloadLog(@RequestBody DownLoadDTO downLoadDTO) throws IOException {
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        String params = "";
        //存储cvmip
        downLoadDTO.setCvmip(downLoadDTO.getIp());
        //如果是容器内的日志，采取容器ip
        if (downLoadDTO.getPath().contains(JsonWordEnum.tsf_default.getJsonWord())) {
            params = callBKInterfaceService.getContainerScriptParams(downLoadDTO, getcontainerip);
            JSONObject getContainerJson = callBKInterfaceService.callLanJingInterface(fastExecuteScriptUrl, params);
            int jobInstanceId = getContainerJson.getJSONObject(JsonWordEnum.data.getJsonWord()).getInteger(JsonWordEnum.job_instance_id.getJsonWord());
            String paramip = callBKInterfaceService.getJobInstanceLogParams(downLoadDTO.getLabel(), jobInstanceId);
            //如果执行成功，查询执行日志
            if (getContainerJson.getBoolean(RESULT) && callBKInterfaceService.isFinish(downLoadDTO.getLabel(), jobInstanceId, 30 * 1000L)) {
                //获取到日志真实容器ip
                JSONObject resultLog = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/", paramip);
                String ip = resultLog.getJSONArray(JsonWordEnum.data.getJsonWord()).getJSONObject(0)
                        .getJSONArray(JsonWordEnum.step_results.getJsonWord()).getJSONObject(0)
                        .getJSONArray(JsonWordEnum.ip_logs.getJsonWord()).getJSONObject(0)
                        .getString(JsonWordEnum.log_content.getJsonWord());
                ip = ip.replaceAll("\n", "");
                //校验ip
                if (Pattern.matches(REGEX_IP_ADDR, ip)){
                    downLoadDTO.setIp(ip);
                } else {
                    response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("蓝鲸获取容器所在ip错误：" + ip)));
                    return;
                }
                //调用落盘脚本
                params = callBKInterfaceService.getContainerScriptParams(downLoadDTO, getcontainerlog);
                JSONObject downloadJson = callBKInterfaceService.callLanJingInterface(fastExecuteScriptUrl, params);
                //确定日志是否落盘
                if (!downloadJson.getBoolean(RESULT) || !callBKInterfaceService.isFinish(downLoadDTO.getLabel(),
                        getContainerJson.getJSONObject(JsonWordEnum.data.getJsonWord()).getInteger(JsonWordEnum.job_instance_id.getJsonWord()), 30 * 1000L)) {
                    response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("蓝鲸落盘超时")));
                    return;
                }
            } else {
                response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("蓝鲸调用执行查询任务超时")));
                return;
            }
        }
        //拼接快速分发文件参数
        params = callBKInterfaceService.getFastPushFile(downLoadDTO);
        JSONObject result = callBKInterfaceService.callLanJingInterface(fastPushFileUrl, params);
        if (result.getBoolean(RESULT)) {
            int jobInstanceId = result.getJSONObject(JsonWordEnum.data.getJsonWord()).getInteger(JsonWordEnum.job_instance_id.getJsonWord());
            String paramsLog = callBKInterfaceService.getJobInstanceLogParams(downLoadDTO.getLabel(), jobInstanceId);
            JSONObject resultLog = new JSONObject();

            long t1 = System.currentTimeMillis();
            //验证执行结果，若未执行完则继续查询，知道查询的作业执行完成
            boolean isFinished = false;
            while (!isFinished) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                resultLog = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/", paramsLog);
                isFinished = (resultLog.getJSONArray(JsonWordEnum.data.getJsonWord()).getJSONObject(0).getBoolean(JsonWordEnum.is_finished.getJsonWord()));

                long t2 = System.currentTimeMillis();
                if (t2 - t1 > 60 * 1000) {
                    if (response != null) {
                        response.setCharacterEncoding("utf-8");
                        response.getWriter()
                                .write(JSONObject.toJSONString(
                                        ServerResponse.failure(
                                                "蓝鲸调用执行分发任务超时， 脚本执行状态为："
                                                + StatusEnum.valueOf("A" + resultLog.getJSONArray(JsonWordEnum.data.getJsonWord()).getJSONObject(0).getIntValue(JsonWordEnum.status.getJsonWord())).getStatus())));
                        return;
                    } else {
                        break;
                    }
                }
            }

            if (resultLog.getBoolean(RESULT)) {
                JSONArray dataArr = resultLog.getJSONArray(JsonWordEnum.data.getJsonWord());
                JSONObject dataObject = dataArr.getJSONObject(0);
                JSONArray stepResultArr = dataObject.getJSONArray(JsonWordEnum.step_results.getJsonWord());
                JSONObject stepResultsObject = stepResultArr.getJSONObject(0);
                JSONArray ipLogs = stepResultsObject.getJSONArray(JsonWordEnum.ip_logs.getJsonWord());
                for (int i = 0; i < ipLogs.size(); i++) {
                    //获取下载文件所需要的参数
                    // JSONObject ip_logsObject = ip_logs.getJSONObject(0);
                    // String ip = ip_logsObject.getString("ip");
                    // String log_content = ip_logsObject.getString("log_content");
                    //验证是否已经传输到文件共享服务器
                    if (resultLog.getJSONArray(JsonWordEnum.data.getJsonWord()).getJSONObject(0).getIntValue(JsonWordEnum.status.getJsonWord()) == StatusEnum.A3.getCode() && !stepResultsObject.toString().contains("is not exist")) {
                        callBKInterfaceService.download(downLoadDTO.getCvmip(), downLoadDTO.getPath(), response);
                    }else if(stepResultsObject.toString().contains("is not exist")) {
                        if (response != null) {
                            response.setCharacterEncoding("utf-8");
                            response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("蓝鲸能够未查询到该文件")));
                            return;
                        }
                    } else {
                        if (response != null) {
                            response.setCharacterEncoding("utf-8");
                            response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("脚本执行失败, 脚本执行状态为：" + StatusEnum.valueOf("A" + resultLog.getJSONArray(JsonWordEnum.data.getJsonWord()).getJSONObject(0).getIntValue(JsonWordEnum.status.getJsonWord())).getStatus())));
                            return;
                        }
                    }
                }
            } else {
                if (response != null) {
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure(resultLog.getString(JsonWordEnum.message.getJsonWord()))));
                    return;
                }
            }
        } else {
            response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure(result.getString("message"))));
            return;
        }
        response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure(result.getString(JsonWordEnum.message.getJsonWord()))));

    }
}
