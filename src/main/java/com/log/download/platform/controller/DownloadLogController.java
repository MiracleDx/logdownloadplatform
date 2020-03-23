package com.log.download.platform.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import java.io.IOException;
import java.util.Objects;


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
        String params = callBKInterfaceService.getFastPushFile(downLoadDTO);
        //调用快速分发文件接口
        JSONObject result = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/fast_push_file/", params);
        if (result.getBoolean("result")) {
            int job_instance_id = result.getJSONObject("data").getInteger("job_instance_id");
            String params_log = callBKInterfaceService.getJobInstanceLogParams(downLoadDTO.getLabel(), job_instance_id);
            JSONObject result_log = new JSONObject();
            long t1 = System.currentTimeMillis();
            //验证执行结果，若未执行完则继续查询，知道查询的作业执行完成
            while (true){

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                result_log = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/", params_log);
                if (result_log.getJSONArray("data").getJSONObject(0).getBoolean("is_finished")) {
                    break;
                }
                long t2 = System.currentTimeMillis();
                if (t2 - t1 > 10 * 1000) {
                    break;
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
                    JSONObject ip_logsObject = ip_logs.getJSONObject(0);
                    String ip = ip_logsObject.getString("ip");
                    String log_content = ip_logsObject.getString("log_content");
                    //验证是否已经传输到文件共享服务器
                    if (log_content.contains("100%")) {
                        callBKInterfaceService.download(ip, downLoadDTO.getPath(), ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse());
                    }
                }
            } else {
                ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse().getWriter().write(JSONObject.toJSONString(ServerResponse.failure(result_log.getString("message"))));
            }
        }
    }


}
