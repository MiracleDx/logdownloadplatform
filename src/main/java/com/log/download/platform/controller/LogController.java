package com.log.download.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.service.CallBKInterfaceService;
import com.log.download.platform.vo.LogDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * LogController
 * 日志查询控制器
 *
 * @author Dongx
 * Description:
 * Created in: 2020-03-16 15:42
 * Modified by:
 */
@Slf4j
@RestController
public class LogController {

    @Resource
    private CallBKInterfaceService callBKInterfaceService;


    /**
     * 查询对应部署组下的日志清单
     *
     * @return
     */
    @PostMapping("/queryLogDetails")
    public ServerResponse<List<LogDetailVO>> queryLogDetails(@RequestBody QueryLogDetailDTO queryLogDetailDTO) {
        //拼接快速执行脚本的参数
        String params = callBKInterfaceService.getFastExecuteScriptParams(queryLogDetailDTO);
        //执行脚本，并获取结果
        JSONObject result = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/fast_execute_script/", params);
        //如果执行成功，查询执行日志
        if (result.getBoolean("result")) {
            int job_instance_id = result.getJSONObject("data").getInteger("job_instance_id");
            String params_log = callBKInterfaceService.getJobInstanceLogParams(queryLogDetailDTO.getLabel(), job_instance_id);
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
                String path = "";
                String ip = "";
                for (int i = 0; i < ip_logs.size(); i++) {
                    JSONObject ip_logs1 = ip_logs.getJSONObject(i);
                    String log_content = ip_logs1.getString("log_content");
                    path += log_content;
                }
                //处理log为路径
                if (path.length() != 0) {
                    String[] paths = path.split("\\n");
                    List<LogDetailVO> list = new ArrayList<>();
                    for (int i = 1; i <= paths.length; i++) {
                        LogDetailVO logDetail = new LogDetailVO();
                        logDetail.setId(i);
                        String[] arr = paths[i-1].split(" ");
                        logDetail.setPath(paths[i - 1]);
                        list.add(logDetail);
                    }
                    return ServerResponse.success(list);
                }
                log.error("无日志文件");
                return ServerResponse.success("无日志文件");
            }
            log.error(result_log.getJSONArray("data").getString(0));
            return ServerResponse.failure(result_log.getJSONArray("data").getString(0));
        }
        log.error(result.getJSONArray("data").getString(0));
        return ServerResponse.failure(result.getJSONArray("data").getString(0));
    }
}
