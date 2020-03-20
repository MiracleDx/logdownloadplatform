package com.log.download.platform.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.service.CallBKInterfaceService;
import com.log.download.platform.vo.LogDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@RestController
public class DownloadLogController {

    @Resource
    private CallBKInterfaceService callBKInterfaceService;

    @RequestMapping("download")
    public void downloadLog(@RequestBody DownLoadDTO downLoadDTO, HttpServletResponse response) {
        String params = callBKInterfaceService.getFastPushFile(downLoadDTO);
        JSONObject result = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/fast_push_file/", params);
        if (result.getBoolean("result")) {
            int job_instance_id = result.getJSONObject("data").getInteger("job_instance_id");
            String params_log = callBKInterfaceService.getJobInstanceLogParams(downLoadDTO.getLabel(), job_instance_id);
            JSONObject result_log = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/", params_log);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            if (result_log.getBoolean("result")) {
                JSONArray data = result_log.getJSONArray("data");
                JSONObject data1 = data.getJSONObject(0);
                JSONArray step_results = data1.getJSONArray("step_results");
                JSONObject step_results1 = step_results.getJSONObject(0);
                JSONArray ip_logs = step_results1.getJSONArray("ip_logs");
                for (int i = 0; i < ip_logs.size(); i++) {
                    JSONObject ip_logs1 = ip_logs.getJSONObject(0);
                    String ip = ip_logs1.getString("ip");
                    String log_content = ip_logs1.getString("log_content");
                    if (log_content.contains("success")) {
                        callBKInterfaceService.download(ip, log_content, response);
                    }
                }
            }
        }
    }


}
