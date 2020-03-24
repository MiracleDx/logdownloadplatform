package com.log.download.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.service.CallBKInterfaceService;
import com.log.download.platform.vo.LogDetailVO;
import com.sun.org.apache.xpath.internal.FoundIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
            //验证执行结果，若未执行完则继续查询，知道查询的作业执行完成
            int job_instance_id = result.getJSONObject("data").getInteger("job_instance_id");
            String params_log = callBKInterfaceService.getJobInstanceLogParams(queryLogDetailDTO.getLabel(), job_instance_id);

            JSONObject result_log = new JSONObject();
            long t1 = System.currentTimeMillis();
            boolean isFinished = false;
            while (!isFinished) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                result_log = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/", params_log);
                isFinished = result_log.getJSONArray("data").getJSONObject(0).getBoolean("is_finished");

                if (result_log.toString().contains("Can not find Agent by ip")) {
                    return ServerResponse.failure(result_log.getString("data"));
                }

                long t2 = System.currentTimeMillis();
                if (t2 - t1 > 15 * 1000) {
                    return ServerResponse.failure("Timeout");
                }
            }

            //获取执行日志
            if (result_log.getBoolean("result")) {
                JSONArray dataArr = result_log.getJSONArray("data");
                JSONObject dataObject = dataArr.getJSONObject(0);
                JSONArray step_resultArr = dataObject.getJSONArray("step_results");
                JSONObject step_resultsObject = step_resultArr.getJSONObject(0);
                JSONArray ip_logs = step_resultsObject.getJSONArray("ip_logs");
                String path = "";
                List<LogDetailVO> list = new ArrayList<>();

                for (int i = 0; i < ip_logs.size(); i++) {
                    JSONObject ip_logs1 = ip_logs.getJSONObject(i);
                    String log_content = ip_logs1.getString("log_content");
                    String ip = ip_logs1.getString("ip");
                    path = log_content;
                    String[] paths = path.split("\\n");
                    // 已存在的logName
                    Map<String, List<LogDetailVO>> map = new HashMap<>();
                    for (int j = 1; j <= paths.length; j++) {
                        LogDetailVO logDetail = new LogDetailVO();
                        logDetail.setId(j);
                        String[] arr = paths[j - 1].split("\t");
                        // 日志路径
                        String logPath = arr[0];
                        logDetail.setPath(logPath);
                        logDetail.setIp(ip);
                        logDetail.setCreateTime(arr[arr.length-1]);
                        logDetail.setLabel(queryLogDetailDTO.getLabel());
                                
                        if (!logPath.contains("---") && (!logPath.contains("No resources found") || !logPath.contains("No such file"))) {
                            // 日志名称
                            String logName = logPath.substring(logPath.lastIndexOf("/"));
                            // 如果key不存在，就新增key和value，否则获取value
                            List<LogDetailVO> vos = map.compute(logName, (k, v) -> {
                                List<LogDetailVO> voList = new ArrayList<>();
                                voList.add(logDetail);
                                return voList;
                            });
                            vos.add(logDetail);
                        }
                    }
                    map.forEach((k, v) -> list.addAll(v.stream()
                            .filter(e -> v.size() > 1 
                                    && !e.getPath().contains("tsf_default") 
                                    && !e.getPath().contains("sys_log"))
                            .distinct().collect(Collectors.toList())));
                }

                if (list.size() == 0) {
                    log.error("无日志文件");
                    return ServerResponse.failure("无日志文件");
                } else {
                    return ServerResponse.success(list);
                }
            }
            log.error(result.getString("message"));
            return ServerResponse.failure(result.getString("message"));
        }
        log.error(result.getString("message"));
        return ServerResponse.failure(result.getString("message"));
    }
}
