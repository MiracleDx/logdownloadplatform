package com.log.download.platform.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.log.download.platform.bo.JobStatusBO;
import com.log.download.platform.common.JsonWordEnum;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.dto.FindMirrorDTO;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.util.BkUtil;
import com.log.download.platform.vo.LogDetailVO;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.partitioningBy;

/**
 * IBaseService
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-10 9:16
 * Modified by:
 */
public interface IBaseService {


    /**
     * @param path
     * @param response
     */
    default void download(String ip, String path, HttpServletResponse response) throws IOException {
        path = "/tmp" + File.separator + "0_" + ip + File.separator + processingContainerRealPath(path);
        // path是指欲下载的文件的路径。
        File file = new File(path);
        if (!file.exists()) {
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(JSONObject.toJSONString(ServerResponse.failure("文件" + path + "不存在")));
        }
        // 取得文件名。
        String filename = file.getName();
        // 取得文件的后缀名。
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(path));
             OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());) {
            // 以流的形式下载文件。
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);

            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
            response.addHeader("Content-Length", "" + file.length());

            response.setContentType("application/octet-stream");
            outputStream.write(buffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 处理容器日志路径
     *
     * @param path tmp[1]=中心名称  c014
     *             tmp[2]=应用名称  01014020
     *             tmp[3]=分公司编码  3300
     *             tmp[4]=部署组id  1
     * @return
     */
    default String processingContainerRealPath(String path) {
        if (path.contains("/tsf_default/") && !path.contains("sys_log.log")) {
            String[] temp = path.split("-");
            path = path.replace("/data/tsf_default/logs", "/log/" + temp[1] + "-" + temp[2] + "-" + temp[3] + "-" + temp[4]);
        }
        return path;
    }

    default JobStatusBO getJonStatus(String lable, int jobInstanceId, RestTemplate restTemplate) {
        JobStatusBO jobStatusBO = new JobStatusBO();
        String paramsLog = BkUtil.getInstance().getJobInstanceLogParams(lable, jobInstanceId);
        JSONObject resultLog = new JSONObject();
        long t1 = System.currentTimeMillis();
        boolean isFinished = false;
        //循环调用查询作业执行情况
        //到达等待时间的阈值，会直接中断
        while (!isFinished) {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            resultLog = BkUtil.getInstance().requestBkInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/", paramsLog, restTemplate);
            isFinished = resultLog.getJSONArray(JsonWordEnum.data.getJsonWord()).getJSONObject(0).getBoolean(JsonWordEnum.is_finished.getJsonWord());
            long t2 = System.currentTimeMillis();
            if (t2 - t1 > 30 * 1000) {
                break;
            }
        }
        jobStatusBO.setIsFinished(isFinished);
        jobStatusBO.setResult(resultLog);
        return jobStatusBO;
    }

    default List<LogDetailVO> getLogDetail(JSONObject result, String lable) {
        JSONArray dataArr = result.getJSONArray(JsonWordEnum.data.getJsonWord());
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
                            logDetail.setLabel(lable);
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
        return list;
    }

    /**
     * 验证日志是否合理存在于日志服务器
     * 按照
     * @param list
     * @return
     */
    default List<LogDetailVO> sortList(List<LogDetailVO> list) {
        List<LogDetailVO> logs = new ArrayList<>();
        for (LogDetailVO log : list) {
            String path = "/tmp" + File.separator + "0_" + log.getIp() + File.separator + processingContainerRealPath(log.getPath());
            log.setMirror(BkUtil.getInstance().getFileIsExists(path, log.getCreateTime()));
            logs.add(log);
        }

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


    /**
     * 查询对应部署组下的日志清单
     *
     * @param queryLogDetailDTO
     * @return
     */
    List<LogDetailVO> queryLogDetails(QueryLogDetailDTO queryLogDetailDTO);

    /**
     * 判断镜像文件是否存在
     *
     * @param findMirrorDTO
     * @return
     */
    Boolean findMirror(FindMirrorDTO findMirrorDTO);

    /**
     * 调用蓝鲸快速分发接口
     * @param downLoadDTO
     */
    void fastPushFile(DownLoadDTO downLoadDTO);
}
