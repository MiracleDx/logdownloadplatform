package com.log.download.platform.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.common.StatusEnum;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.service.CallBKInterfaceService;
import lombok.extern.slf4j.Slf4j;
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

import static com.log.download.platform.controller.LogController.DATA;


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
        //调用快速分发文件接口
        if (downLoadDTO.getPath().contains("/tsf_default/")) {
            params = callBKInterfaceService.getContainerScriptParams(downLoadDTO);
            JSONObject getContainerJson = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/fast_execute_script/", params);
            //如果执行成功，查询执行日志
            if (getContainerJson.getBoolean(RESULT)) {
                //验证执行结果，若未执行完则继续查询，知道查询的作业执行完成
                int jobInstanceId = getContainerJson.getJSONObject(DATA).getInteger("job_instance_id");
                String paramsLog = callBKInterfaceService.getJobInstanceLogParams(downLoadDTO.getLabel(), jobInstanceId);

                JSONObject resultLog = new JSONObject();
                long t1 = System.currentTimeMillis();
                boolean isFinished = false;
                while (!isFinished) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    resultLog = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/", paramsLog);
                    isFinished = resultLog.getJSONArray(DATA).getJSONObject(0).getBoolean("is_finished");

                    long t2 = System.currentTimeMillis();
                    if (t2 - t1 > 30 * 1000) {
                        response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("蓝鲸调用执行查询任务超时")));
                    }
                }
            }
        }
        params = callBKInterfaceService.getFastPushFile(downLoadDTO);
        JSONObject result = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/fast_push_file/", params);
        if (result.getBoolean(RESULT)) {
            int jobInstanceId = result.getJSONObject("data").getInteger("job_instance_id");
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
                isFinished = (resultLog.getJSONArray("data").getJSONObject(0).getBoolean("is_finished"));

                long t2 = System.currentTimeMillis();
                if (t2 - t1 > 60 * 1000) {
                    if (response != null) {
                        response.setCharacterEncoding("utf-8");
                        response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("蓝鲸调用执行分发任务超时， 脚本执行状态为：" + StatusEnum.valueOf("A" + resultLog.getJSONArray("data").getJSONObject(0).getIntValue("status")).getStatus())));
                        return;
                    } else {
                        break;
                    }
                }
            }

            if (resultLog.getBoolean(RESULT)) {
                JSONArray dataArr = resultLog.getJSONArray("data");
                JSONObject dataObject = dataArr.getJSONObject(0);
                JSONArray stepResultArr = dataObject.getJSONArray("step_results");
                JSONObject stepResultsObject = stepResultArr.getJSONObject(0);
                JSONArray ipLogs = stepResultsObject.getJSONArray("ip_logs");
                for (int i = 0; i < ipLogs.size(); i++) {
                    //获取下载文件所需要的参数
                    // JSONObject ip_logsObject = ip_logs.getJSONObject(0);
                    // String ip = ip_logsObject.getString("ip");
                    // String log_content = ip_logsObject.getString("log_content");
                    //验证是否已经传输到文件共享服务器
                    if (resultLog.getJSONArray("data").getJSONObject(0).getIntValue("status") == StatusEnum.A3.getCode()) {
                        callBKInterfaceService.download(downLoadDTO.getIp(), downLoadDTO.getPath(), response);
                    } else {
                        if (response != null) {
                            response.setCharacterEncoding("utf-8");
                            response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("脚本执行失败, 脚本执行状态为：" + StatusEnum.valueOf("A" + resultLog.getJSONArray("data").getJSONObject(0).getIntValue("status")).getStatus())));
                        }
                    }
                }
            } else {
                if (response != null) {
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure(resultLog.getString("message"))));
                }
            }
        } else {
            response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure(result.getString("message"))));
        }
        response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure(result.getString("message"))));

    }
}
