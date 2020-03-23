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

    @Resource
    private CallBKInterfaceService callBKInterfaceService;

    @RequestMapping("/download")
    public void downloadLog(@RequestBody DownLoadDTO downLoadDTO) throws IOException {
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        String params = callBKInterfaceService.getFastPushFile(downLoadDTO);
        //调用快速分发文件接口
        JSONObject result = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/fast_push_file/", params);
        if (result.getBoolean("result")) {
            int job_instance_id = result.getJSONObject("data").getInteger("job_instance_id");
            String params_log = callBKInterfaceService.getJobInstanceLogParams(downLoadDTO.getLabel(), job_instance_id);
            JSONObject result_log = new JSONObject();

            long t1 = System.currentTimeMillis();
            //验证执行结果，若未执行完则继续查询，知道查询的作业执行完成
            boolean isFinished = false;
            while (!isFinished){
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                result_log = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/", params_log);
                isFinished = (result_log.getJSONArray("data").getJSONObject(0).getBoolean("is_finished"));

                long t2 = System.currentTimeMillis();
                if (t2 - t1 > 30 * 1000) {
                    if (response != null) {
                        response.setCharacterEncoding("utf-8");
                        response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("执行分发任务超时， 脚本执行状态为：" + StatusEnum.valueOf("A" + result_log.getJSONArray("data").getJSONObject(0).getIntValue("status")).getStatus())));
                        return;
                    } else {
                        break;
                    }
                }
            }
            
            if (result_log.getBoolean("result")) {
                JSONArray dataArr = result_log.getJSONArray("data");
                JSONObject dataObject = dataArr.getJSONObject(0);
                JSONArray step_resultArr = dataObject.getJSONArray("step_results");
                JSONObject step_resultsObject = step_resultArr.getJSONObject(0);
                JSONArray ip_logs = step_resultsObject.getJSONArray("ip_logs");
                for (int i = 0; i < ip_logs.size(); i++) {
                    //获取下载文件所需要的参数
                    // JSONObject ip_logsObject = ip_logs.getJSONObject(0);
                    // String ip = ip_logsObject.getString("ip");
                    // String log_content = ip_logsObject.getString("log_content");
                    //验证是否已经传输到文件共享服务器
                    if (result_log.getJSONArray("data").getJSONObject(0).getIntValue("status") == StatusEnum.A3.getCode()) {
                        callBKInterfaceService.download(downLoadDTO.getIp(), downLoadDTO.getPath(), response);
                    } else {
                        if (response != null) {
                            response.setCharacterEncoding("utf-8");
                            response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("脚本执行失败, 脚本执行状态为：" + StatusEnum.valueOf("A" + result_log.getJSONArray("data").getJSONObject(0).getIntValue("status")).getStatus())));
                        }
                    }
                }
            } else {
                if (response != null) {
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure(result_log.getString("message"))));
                }
            }
        }
    }


}
