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
import java.util.*;
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

    /**
     * 调用接口返回的执行结果
     */
    public static final String RESULT = "result";
    public static final String DATA = "data";

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
        JSONObject resultObject = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/fast_execute_script/", params);
        //如果执行成功，查询执行日志
        if (resultObject.getBoolean(RESULT)) {
            //验证执行结果，若未执行完则继续查询，知道查询的作业执行完成
            int jobInstanceId = resultObject.getJSONObject(DATA).getInteger("job_instance_id");
            String paramsLog = callBKInterfaceService.getJobInstanceLogParams(queryLogDetailDTO.getLabel(), jobInstanceId);

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

                if (resultLog.toString().contains("Can not find Agent by ip")) {
                    return ServerResponse.failure(resultLog.getString("data"));
                }

                long t2 = System.currentTimeMillis();
                if (t2 - t1 > 30 * 1000) {
                    return ServerResponse.failure("蓝鲸调用执行查询任务超时");
                }
            }

            //获取执行日志
            if (resultLog.getBoolean(RESULT)) {
                JSONArray dataArr = resultLog.getJSONArray(DATA);
                JSONObject dataObject = dataArr.getJSONObject(0);
                List<LogDetailVO> list = new ArrayList<>();
                String notFinished;
                JSONArray stepResultArr = dataObject.getJSONArray("step_results");
                for (int o = 0; o < stepResultArr.size(); o++) {
                    JSONObject stepResultsObject = stepResultArr.getJSONObject(o);
                    JSONArray ipLogs = stepResultsObject.getJSONArray("ip_logs");
                    String path;
                    for (int i = 0; i < ipLogs.size(); i++) {
                        JSONObject ipLogs1 = ipLogs.getJSONObject(i);
                        String logContent = ipLogs1.getString("log_content");
                        String ip = ipLogs1.getString("ip");
                        path = logContent;
                        String[] paths = path.split("\\n");
                        // 已存在的logName
                        Map<String, List<LogDetailVO>> map = new HashMap<>(16);
                        for (int j = 1; j <= paths.length; j++) {
                            LogDetailVO logDetail = new LogDetailVO();
                            logDetail.setId(j);
                            String[] arr = paths[j - 1].split("\t");
                            // 日志路径
                            String logPath = arr[0];
                            if (!logPath.contains("---")
                                    && !logPath.contains("No resources found")
                                    && !logPath.contains("No such file")
                                    && !logPath.contains("Unable to connect to the server")
                                    && !logPath.contains("certificate is valid")) {
                                logDetail.setPath(logPath);
                                logDetail.setIp(ip);
                                logDetail.setCreateTime(arr[arr.length - 1]);
                                logDetail.setSize(Math.round(Double.parseDouble(arr[arr.length - 2]) * 100 / (1024 * 1024)) / 100.0 + "M");
                                logDetail.setLabel(queryLogDetailDTO.getLabel());
                                // 日志名称
                                String logName = logPath.substring(logPath.lastIndexOf("/"));
                                // 过滤torrent文件
                                if (!logName.contains("torrent")) {
                                    // 如果key不存在，就新增key和value，否则获取value
                                    List<LogDetailVO> vos = map.compute(logName, (k, v) -> {
                                        List<LogDetailVO> voList = new ArrayList<>();
                                        voList.add(logDetail);
                                        return voList;
                                    });
                                    vos.add(logDetail);
                                }
                            }
                        }

                        map.forEach((k, v) -> {
                            // 多个同名日志
                            if (v.size() > 1) {
                                // 如果包含落盘日志
                                if (v.stream().anyMatch(e -> e.getPath().contains("/log/"))) {
                                    // 存入落盘日志和sys_log
                                    list.addAll(v.stream().filter(e -> !e.getPath().contains("tsf_default") || e.getPath().contains("sys_log")).distinct().collect(Collectors.toList()));
                                } else {
                                    list.addAll(v.stream().distinct().collect(Collectors.toList()));
                                }
                            } else {
                                list.addAll(v);
                            }
                        });
                    }

                    if (list.size() == 0) {
                        log.error("蓝鲸查询无日志文件列表返回");
                        return ServerResponse.failure("蓝鲸查询无日志文件列表返回");
                    } else {
                        List<LogDetailVO> logs = callBKInterfaceService.getFileIsExists(list);
                        Collections.sort(logs);
                        Collections.reverse(logs);
                        return ServerResponse.success(logs);
                    }
                }
            }
            log.error(resultObject.getString("message"));
            return ServerResponse.failure(resultObject.getString("message"));
        }
        log.error(resultObject.getString("message"));
        return ServerResponse.failure(resultObject.getString("message"));
    }
}
