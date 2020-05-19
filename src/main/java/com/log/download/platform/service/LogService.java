package com.log.download.platform.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.bo.JobStatusBO;
import com.log.download.platform.bo.LogPathBO;
import com.log.download.platform.common.BkConstant;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.support.FileCheckExecutors;
import com.log.download.platform.util.BkUtil;
import com.log.download.platform.util.FileUtil;
import com.log.download.platform.util.LogUtil;
import com.log.download.platform.vo.LogDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.partitioningBy;

/**
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-04-13 14:37
 **/
@Slf4j
@Service
public class LogService {

    @Value("${fast_execute_scriptId}")
    private int fastExecuteScriptId;

    @Value("${fast_execute_history_scriptId}")
    private int fastExecuteHistoryScriptId;

    @Value("${gateway_execute_scriptId}")
    private int gatewayExecuteScriptId;

    @Value("${gateway_execute_history_scriptId}")
    private int gatewayExecuteHistoryScriptId;

    @Resource
    private RestTemplate restTemplate;

    public LogPathBO queryLogDetails(QueryLogDetailDTO queryLogDetailDTO) {
        //获取日志路径的接口参数
        BkUtil bkUtil = BkUtil.getInstance();
        FileUtil fileUtil = FileUtil.getInstance();
        int script_id = 0;
        //根据脚本入参的参数，判断是否网关，选择脚本id
        if(queryLogDetailDTO.getBkParam().contains("msgw")) {
            if (queryLogDetailDTO.getIsHistory()) {
                script_id = gatewayExecuteHistoryScriptId;
            } else {
                script_id = gatewayExecuteScriptId;
            }
        } else {
            if (queryLogDetailDTO.getIsHistory()) {
                script_id = fastExecuteHistoryScriptId;
            }  else {
                script_id = fastExecuteScriptId;
            }
        }
        log.info("执行脚本id：{}", script_id);
        int bkBizId = bkUtil.getBkBizId(queryLogDetailDTO.getLabel());
        String fastExecuteParam = bkUtil.getFastExecuteScriptParams(bkBizId,
                queryLogDetailDTO.getIps(),
                queryLogDetailDTO.getBkParam(),
                script_id);
        //快速执行脚本，结果中获取作业id，用于查询结果
        Integer jobInstanceId = bkUtil.getJobInstanceId(fastExecuteParam, restTemplate);
        //查询作业执行状态
        JobStatusBO jobStatus = bkUtil.getJobStatus(bkBizId, jobInstanceId, restTemplate);

        //如果脚本执行完成，将结果的log信息进行提取
        if (jobStatus.getIsFinished()) {
            List<LogDetailVO> logPathList = new ArrayList<>();
            LogPathBO logPathBO = LogPathBO.builder().list(logPathList).notFinish("").build();
            JSONObject dataObject = jobStatus.getResult().
                    getJSONArray(BkConstant.DATA).getJSONObject(0);
            JSONArray stepResultArr = dataObject.getJSONArray(BkConstant.STEP_RESULTS);
            for (int i = 0; i < stepResultArr.size(); i++) {
                JSONObject stepResultsObject = stepResultArr.getJSONObject(i);
                JSONArray ipLogs = stepResultsObject.getJSONArray(BkConstant.IP_LOGS);
                int ipStatus = stepResultsObject.getInteger(BkConstant.IP_STATUS);
                for (int j = 0; j < ipLogs.size(); j++) {
                    JSONObject ipLogsObject = ipLogs.getJSONObject(j);
                    logPathBO = logJsonToList(logPathBO, ipLogsObject, ipStatus, queryLogDetailDTO.getLabel());
                }
            }

            CountDownLatch latch = new CountDownLatch(logPathBO.getList().size());
            //判断文件是否已经存在于文件服务器上
            FileCheckExecutors fileChecker = FileCheckExecutors.getInstance();
            logPathBO.getList().forEach(e ->
                fileChecker.execute(() -> {
                    // 校验日志路径
                    String path = LogUtil.getInstance().processingCvmPath(e.getPath(), e.getHostname());
                    Boolean fileIsExists = fileUtil.getFileIsExists(path, e.getCreateTime());
                    log.debug("check file path: {}, exists: {}", path, fileIsExists);
                    e.setMirror(fileIsExists);
                    latch.countDown();
                })
            );

            try {
                latch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("CountDownLatch occurred exception: ", e);
            }

            // 赋值排序后的引用
            logPathBO.setList(sortLogs(logPathBO.getList().parallelStream().distinct().collect(Collectors.toList())));
            String notFinishedIp = "";
            if (!"".equals(logPathBO.getNotFinish()) && logPathBO.getNotFinish().length() > 0){
                notFinishedIp = logPathBO.getNotFinish().substring(0, logPathBO.getNotFinish().length() - 1);
            }
            logPathBO.setNotFinish(notFinishedIp);
            return logPathBO;
        }
        return LogPathBO.builder().list(new ArrayList<>()).notFinish("").build();
    }

    /**
     * 将结果json中的日志路径数据提取出来
     * @param logPathBO
     * @param jsonObject
     * @param ipStatus
     * @param label
     * @return
     */
    public LogPathBO logJsonToList(LogPathBO logPathBO, JSONObject jsonObject, int ipStatus, String label){
        LogUtil logUtil = LogUtil.getInstance();
        String logContent = jsonObject.getString(BkConstant.LOG_CONTENT);
        String ip = jsonObject.getString(BkConstant.IP);
        int errorCode = jsonObject.getInteger(BkConstant.ERROR_CODE);
        List<LogDetailVO> list = logPathBO.getList();
        StringBuilder notFinished = new StringBuilder(logPathBO.getNotFinish());
        String path;
        if (ipStatus == 9 && errorCode == 0 && !logContent.contains("Can not find Agent by ip")) {
            path = logContent;
            String[] paths = path.split("\\n");
            // 已存在的logName
            Map<String, List<LogDetailVO>> map = new HashMap<>(16);
            for (int z = 1; z <= paths.length; z++) {
                LogDetailVO logDetail = new LogDetailVO();
                logDetail.setId(z);
                String[] logArr = paths[z - 1].split("\t");
                // 这里区分出微服务和微服务容器
                // 日志路径
                String logPath ="";
                if (!FileUtil.getInstance().pathLegal(paths[z - 1])) {
                    continue;
                }
                LogUtil.LogEnum logEnum = LogUtil.getInstance().placeWay(paths[z - 1]);
                //todo 区分落盘和为落盘日志
                if (logEnum == LogUtil.LogEnum.gateway_container && logArr.length == 4) {
                    //容器网关日志
                    logPath = logUtil.praseGatewayContainerLogDetail(logArr);
                } else if (logEnum == LogUtil.LogEnum.gateway_general) {
                    //网关日志
                    logPath = logUtil.praseGatewayLogDetail(logArr);
                } else if (logEnum == LogUtil.LogEnum.server_container && logArr.length == 4) {
                    //容器微服务日志
                    logPath = logUtil.praseServerContainerLogDetail(logArr);
                    if (!logUtil.checkHostName(logPath,logArr[0])) {
                        continue;
                    }
                } else if(logEnum == LogUtil.LogEnum.server_general) {
                    //微服务日志
                    logPath = logUtil.praseServerLogDetail(logArr);
                }else {
                    continue;
                }
                if (FileUtil.getInstance().pathLegal(logPath)) {
                    // todo 增加LogPathBO中脚本入参的属性
                    logDetail.setHostname(logArr[0]);
                    logDetail.setPath(logPath);
                    logDetail.setIp(ip);
                    logDetail.setCreateTime(logArr[logArr.length - 1]);
                    try {
                        logDetail.setSize(Math.round(Double.parseDouble(logArr[logArr.length - 2]) * 100 / (1024 * 1024)) / 100.0);
                    } catch (Exception e) {
                        log.error("日志路径数据异常：{}", paths[z - 1]);
                    }
                    logDetail.setUnit("M");
                    logDetail.setLabel(label);
                    // 日志名称
                    //String logName = logPath.substring(logPath.lastIndexOf("/"));
                    // 如果key不存在，就新增key和value，否则获取value
                    //map.compute(logName, (k, v) -> {
                    //    List<LogDetailVO> voList = new ArrayList<>();
                    //    voList.add(logDetail);
                    //    return voList;
                    //}).add(logDetail);
                    // 显示全部日志
                    list.add(logDetail);
                }
            }
            //map.forEach((k, v) -> {
            //    // 多个同名日志
            //    if (v.size() > 1) {
            //        // 如果包含落盘日志
            //        if (v.stream().anyMatch(e -> e.getPath().contains("/log/"))) {
            //            // 存入落盘日志和sys_log
            //            list.addAll(v.stream().filter(e -> !e.getPath().contains("tsf_default") || e.getPath().contains("sys_log")).distinct().collect(Collectors.toList()));
            //        } else {
            //            list.addAll(v.stream().distinct().collect(Collectors.toList()));
            //        }
            //    } else {
            //        list.addAll(v);
            //    }
            //});

            // 显示全部日志
            list = list.parallelStream().distinct().collect(Collectors.toList());
        } else {
            if (!notFinished.toString().contains(ip)) {
                notFinished.append(ip).append(",");
            }
        }
        logPathBO.setList(list);
        logPathBO.setNotFinish(notFinished.toString());
        return logPathBO;
    }


    /**
     * 日志排序
     * @param logs
     * @return
     */
    public List<LogDetailVO> sortLogs(List<LogDetailVO> logs) {
        Map<Boolean, List<LogDetailVO>> collect = logs.stream().collect(partitioningBy(data -> data.getSize() > 0));
        // 日志大小大于0的日志
        List<LogDetailVO> gtZero = collect.get(true);
        // 日志大小等于0的日志
        List<LogDetailVO> eqZero = collect.get(false);
        // 对日志大小大于0的日志逆序
        logs = gtZero.stream().sorted(Comparator.comparing(LogDetailVO::getCreateTime).reversed()).collect(Collectors.toList());
        // 添加日志大小等于0的日志
        logs.addAll(eqZero);
        return logs;
    }

    public boolean findFile(String logPath, String ip, String hostname) {
        if (!logPath.isEmpty()) {
            String path;
            System.out.println(hostname);
            logPath = LogUtil.getInstance().processingCvmPath(logPath, hostname);
            path = "/tmp" + File.separator + "0_" + ip + File.separator + logPath;
            log.info("查找日志镜像路径: {}", path);
            File file = new File(path);
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

}
