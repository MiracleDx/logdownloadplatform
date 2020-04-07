package com.log.download.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.common.JsonWordEnum;
import com.log.download.platform.dto.FindMirrorDTO;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.response.ResponseCode;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.service.CallBKInterfaceService;
import com.log.download.platform.vo.LogDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.log.download.platform.response.ResponseCode.PARTIAL_DATA_NOT_FOUND;
import static java.util.stream.Collectors.partitioningBy;

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
        JSONObject resultObject = callBKInterfaceService.callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/fast_execute_script/", params);
        //如果执行成功，查询执行日志
        if (resultObject.getBoolean(JsonWordEnum.result.getJsonWord())) {
            //验证执行结果，若未执行完则继续查询，知道查询的作业执行完成
            int jobInstanceId = resultObject.getJSONObject(JsonWordEnum.data.getJsonWord()).getInteger(JsonWordEnum.job_instance_id.getJsonWord());
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
                isFinished = resultLog.getJSONArray(JsonWordEnum.data.getJsonWord()).getJSONObject(0).getBoolean(JsonWordEnum.is_finished.getJsonWord());

//                if (resultLog.toString().contains("Can not find Agent by ip")) {
//                    return ServerResponse.failure(resultLog.getString("data"));
//                }

                long t2 = System.currentTimeMillis();
                if (t2 - t1 > 30 * 1000) {
                    return ServerResponse.failure("蓝鲸调用执行查询任务超时");
                }
            }

            //获取执行日志
            if (resultLog.getBoolean(JsonWordEnum.result.getJsonWord())) {
                JSONArray dataArr = resultLog.getJSONArray(JsonWordEnum.data.getJsonWord());
                JSONObject dataObject = dataArr.getJSONObject(0);
                List<LogDetailVO> list = new ArrayList<>();
                StringBuilder notFinished = new StringBuilder();
                JSONArray stepResultArr = dataObject.getJSONArray(JsonWordEnum.step_results.getJsonWord());
                for (int o = 0; o < stepResultArr.size(); o++) {
                    JSONObject stepResultsObject = stepResultArr.getJSONObject(o);
                    JSONArray ipLogs = stepResultsObject.getJSONArray(JsonWordEnum.ip_logs.getJsonWord());
                    int ipStatus = stepResultsObject.getInteger(JsonWordEnum.ip_status.getJsonWord());
                    String path;
                    for (int i = 0; i < ipLogs.size(); i++) {
                        JSONObject ipLogs1 = ipLogs.getJSONObject(i);
                        String logContent = ipLogs1.getString(JsonWordEnum.log_content.getJsonWord());
                        String ip = ipLogs1.getString("ip");
                        int errorCode = ipLogs1.getInteger("error_code");
                        if (ipStatus == 9 && errorCode == 0 && !logContent.contains("Can not find Agent by ip")) {
                            path = logContent;
                            String[] paths = path.split("\\n");
                            // 已存在的logName
                            Map<String, List<LogDetailVO>> map = new HashMap<>(16);
                            for (int j = 1; j <= paths.length; j++) {
                                LogDetailVO logDetail = new LogDetailVO();
                                logDetail.setId(j);
                                String[] arr = paths[j - 1].split("\t");
                                // 日志路径
                                String logPath = StringUtils.isEmpty(arr[0]) ? arr[1] : arr[0];
                                if (!StringUtils.isEmpty(logPath) && !logPath.contains("---")
                                        && !logPath.contains("error: ")
                                        && !logPath.contains("No resources found")
                                        && !logPath.contains("No such file")
                                        && !logPath.contains("Unable to connect to the server")
                                        && !logPath.contains("Error from server")
                                        && !logPath.contains("cannot exec into a container")
                                        && !logPath.contains("command terminated with exit code 1")
                                        && !logPath.contains("certificate is valid")) {
                                    logDetail.setPath(logPath);
                                    logDetail.setIp(ip);
                                    logDetail.setCreateTime(arr[arr.length - 1]);
                                    logDetail.setSize(Math.round(Double.parseDouble(arr[arr.length - 2]) * 100 / (1024 * 1024)) / 100.0);
                                    logDetail.setUnit("M");
                                    logDetail.setLabel(queryLogDetailDTO.getLabel());
                                    // 日志名称
                                    String logName = logPath.substring(logPath.lastIndexOf("/"));
                                    // 过滤torrent文件
                                    if (!logName.contains("torrent")) {
                                        // 如果key不存在，就新增key和value，否则获取value
                                        map.compute(logName, (k, v) -> {
                                            List<LogDetailVO> voList = new ArrayList<>();
                                            voList.add(logDetail);
                                            return voList;
                                        }).add(logDetail);
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
                        } else {
                            if (!notFinished.toString().contains(ip)) {
                                notFinished.append(ip).append(",");
                            }
                        }
                    }
                }

                if (list.size() == 0) {
                    log.error("蓝鲸查询无日志文件列表返回");
                    return ServerResponse.failure("蓝鲸查询无日志文件列表返回");
                } else {
                    List<LogDetailVO> logs = callBKInterfaceService.getFileIsExists(list);

                    Map<Boolean, List<LogDetailVO>> collect = logs.stream().collect(partitioningBy(data -> data.getSize() > 0));
                    // 日志大小大于0的日志
                    List<LogDetailVO> gtZero = collect.get(true);
                    // 日志大小等于0的日志
                    List<LogDetailVO> eqZero = collect.get(false);
                    // 对日志大小大于0的日志逆序
                    logs = gtZero.stream().sorted(Comparator.comparing(LogDetailVO::getCreateTime).reversed()).collect(Collectors.toList());
                    // 添加日志大小等于0的日志
                    logs.addAll(eqZero);
                    
                    if (!"".equals(notFinished.toString()) && notFinished.length() > 0){
                        return ServerResponse.failure(PARTIAL_DATA_NOT_FOUND.code(), notFinished.substring(0, notFinished.length() - 1), logs);
                    }
                    return ServerResponse.success(logs);
                }
            }
            log.error(resultObject.getString(JsonWordEnum.message.getJsonWord()));
            return ServerResponse.failure(resultObject.getString(JsonWordEnum.message.getJsonWord()));
        }
        log.error(resultObject.getString(JsonWordEnum.message.getJsonWord()));
        return ServerResponse.failure(resultObject.getString(JsonWordEnum.message.getJsonWord()));
    }

    /**
     * 回调函数，查询镜像是否已经存在
     * @param findMirrorDTO
     * @return
     */
    @PostMapping("/findMirror")
    public ServerResponse<List<LogDetailVO>> findMirror(@RequestBody FindMirrorDTO findMirrorDTO){
        if (!"".equals(findMirrorDTO.getPath()) && findMirrorDTO.getPath() != null) {
            String path = "/tmp" + File.separator + "0_" + findMirrorDTO.getIp() + File.separator + findMirrorDTO.getPath();
            File file = new File(path);
            if (file.exists()){
                return ServerResponse.success();
            }
            return ServerResponse.failure(ResponseCode.DATA_NOT_FOUND.getCode(), ResponseCode.DATA_NOT_FOUND.getMessage());
        }
        return ServerResponse.failure("传送路径失败");
    }
}
