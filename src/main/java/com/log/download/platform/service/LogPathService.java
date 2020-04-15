package com.log.download.platform.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.bo.JobStatusBO;
import com.log.download.platform.bo.LogPathBO;
import com.log.download.platform.common.BkConstant;
import com.log.download.platform.dto.FindMirrorDTO;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.response.ResponseCode;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.util.BkUtil;
import com.log.download.platform.util.FileUtil;
import com.log.download.platform.util.LogUtil;
import com.log.download.platform.vo.LogDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.log.download.platform.response.ResponseCode.PARTIAL_DATA_NOT_FOUND;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

/**
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-04-13 14:37
 **/
@Slf4j
@Service
public class LogPathService {

    @Value("${fast_execute_scriptid}")
    private int fast_execute_scriptid;

    @Value("${fast_execute_history_scriptId}")
    private int fast_execute_history_scriptId;

    @Value("${msgw_scriptId}")
    private int msgw_scriptId;

    @Value("${msgw_history_scriptId}")
    private int msgw_history_scriptId;
    
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
                script_id = msgw_history_scriptId;
            } else {
                script_id = msgw_scriptId;
            }
        } else {
            if (queryLogDetailDTO.getIsHistory()) {
                script_id = fast_execute_history_scriptId;
            }  else {
                script_id = fast_execute_scriptid;
            }
        }
        int bkBizId = bkUtil.getBkBizId(queryLogDetailDTO.getLabel());
        String fastExecuteParam = bkUtil.getFastExecuteScriptParams(bkBizId,
                queryLogDetailDTO.getIps(),
                queryLogDetailDTO.getBkParam(),
                script_id);
        //快速执行脚本，结果中获取作业id，用于查询结果
        Integer jobInstanceId = bkUtil.getJobInstanceId(fastExecuteParam, restTemplate);
        //查询作业执行状态
        JobStatusBO jobStatus = bkUtil.getJobStatus(bkBizId, jobInstanceId, restTemplate);
        //如果脚本执行完成，将结果的json进行提取
        if (jobStatus.getIsFinished()) {
            List<LogDetailVO> logpathlist = new ArrayList<>();
            LogPathBO logPathBO = LogPathBO.builder().list(logpathlist).notFinish("").build();
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
            //判断文件是否已经存在于文件服务器上
            logPathBO.getList().parallelStream().forEach(e -> {
                // 校验日志路径
                String path = LogUtil.getInstance().processingCvmPath(e.getPath());
                Boolean fileIsExists = FileUtil.getInstance().getFileIsExists(path, e.getCreateTime());
                e.setMirror(fileIsExists);
            });
            sortLogs(logPathBO.getList());
            String notFinishedIp = "";
            if (!"".equals(logPathBO.getNotFinish()) && logPathBO.getNotFinish().length() > 0){
                notFinishedIp = logPathBO.getNotFinish().substring(0, logPathBO.getNotFinish().length() - 1);
            }
            logPathBO.setNotFinish(notFinishedIp);
            return logPathBO;
        }
        return null;
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
                String[] arr = paths[z - 1].split("\t");
                // 日志路径
                String logPath = StringUtils.isEmpty(arr[0]) ? arr[1] : arr[0];
                if (FileUtil.getInstance().pathLegal(logPath)) {
                    logDetail.setPath(logPath);
                    logDetail.setIp(ip);
                    logDetail.setCreateTime(arr[arr.length - 1]);
                    logDetail.setSize(Math.round(Double.parseDouble(arr[arr.length - 2]) * 100 / (1024 * 1024)) / 100.0);
                    logDetail.setUnit("M");
                    logDetail.setLabel(label);
                    // 日志名称
                    String logName = logPath.substring(logPath.lastIndexOf("/"));
                    // 如果key不存在，就新增key和value，否则获取value
                    map.compute(logName, (k, v) -> {
                        List<LogDetailVO> voList = new ArrayList<>();
                        voList.add(logDetail);
                        return voList;
                    }).add(logDetail);
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

    public boolean findFile(String logPath, String ip) {
        if (!logPath.isEmpty()) {
            String path;
            logPath = LogUtil.getInstance().processingContainerRealPath(logPath);
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
