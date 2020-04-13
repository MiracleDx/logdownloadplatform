package com.log.download.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.common.BkEnum;
import com.log.download.platform.common.JsonWordEnum;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.dto.HostDTO;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.vo.LogDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * CallBKInterfaceService
 * 蓝鲸接口调用
 *
 * @author YaoDF
 * Description:
 * Created in: 2020-03-16 15:42
 * Modified by:
 */
@Slf4j
@Service
public class CallBKInterfaceService {


    public static final int TWOHOURS = 120;

    @Value("${bk_app_code}")
    private String bk_app_code;

    @Value("${bk_app_secret}")
    private String bk_app_secret;

    @Value("${bk_username}")
    private String bk_username;

    @Value("${getlogpath_script_id}")
    private int script_id;

    @Value("${historyScriptID}")
    private int historyScriptID;

    @Value("${getcontainerlog}")
    private int getcontainerlog;

    @Value("${msgwScriptID}")
    private int msgwScriptID;

    @Value("${historymsgwScriptID}")
    private int historymsgwScriptID;

    /**
     * base64编码
     */
    final Base64.Encoder encoder = Base64.getEncoder();


    /**
     * 根据传参和url调用蓝鲸接口
     * @param url
     * @param params
     * @return
     */
    public JSONObject callLanJingInterface(String url, String params) {
        RestTemplate restTemplate = new RestTemplate();
        //请求头
        HttpHeaders headers = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(params, headers);
        //发送请求调用接口
        ResponseEntity<String> request = restTemplate.postForEntity(url, httpEntity, String.class);
        JSONObject resultData = JSONObject.parseObject(request.getBody());
        return resultData;
    }

    /**
     * 获取快速执行脚本入参
     *
     * @param queryLogDetailDTO
     * @return
     */
    public String getFastExecuteScriptParams(QueryLogDetailDTO queryLogDetailDTO) {
        String label = queryLogDetailDTO.getLabel();
        String[] ips = queryLogDetailDTO.getIps();
        String param = queryLogDetailDTO.getBkParam();
        BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
        int bk_biz_id = bkEnum.getCode();
        byte[] content = param.getBytes();
        String script_param = encoder.encodeToString(content);
        int fastExecuteScript_id = script_id;
        //根据脚本入参的参数，判断是否网关，选择脚本id
        if(param.contains("msgw")) {
            if (queryLogDetailDTO.getIsHistory()) {
                fastExecuteScript_id = historymsgwScriptID;
            } else {
                fastExecuteScript_id = msgwScriptID;
            }
        } else {
            if (queryLogDetailDTO.getIsHistory()) {
                fastExecuteScript_id = historyScriptID;
            }  else {
                fastExecuteScript_id = script_id;
            }
        }
        //拼接接口入参
        String params = "{\n" +
                "\t\"bk_app_code\": \"" + bk_app_code + "\",\n" +
                "\t\"bk_app_secret\": \"" + bk_app_secret + "\",\n" +
                "\t\"bk_username\": \"" + bk_username + "\",\n" +
                "\t\"bk_biz_id\": " + bk_biz_id + ",\n" +
                "\t\"script_id\": " + fastExecuteScript_id + ",\n" +
                "\t\"script_param\": \"" + script_param + "\",\n" +
                "\t\"script_timeout\": 1000,\n" +
                "\t\"account\": \"ubuntu\",\n" +
                "\t\"is_param_sensitive\": 0,\n" +
                "}";
        List<Map<String, Object>> list = new ArrayList<>();
        HostDTO hostDTO = new HostDTO();
        for (String ip : ips) {
            Map<String, Object> hostMap = new HashMap<>();
            hostMap.put("bk_cloud_id", 0);
            hostMap.put("ip", ip);
            list.add(hostMap);
            hostDTO.setIp_list(list);
        }
        String ipListJson = JSONObject.toJSON(hostDTO).toString();
        // 合并两段JSON
        JSONObject json = JSONObject.parseObject(params);
        JSONObject ipJson = JSONObject.parseObject(ipListJson);
        JSONObject resultParam = new JSONObject();
        resultParam.putAll(json);
        resultParam.putAll(ipJson);
        String paramResult = resultParam.toJSONString();

        return paramResult;
    }

    /**
     * 拼接作业执行日志接口入参
     * @param label
     * @param job_instance_id
     * @return
     */
    public String getJobInstanceLogParams(String label, int job_instance_id) {
        BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
        int bk_biz_id = bkEnum.getCode();
        String params = "{\n" +
                "\t\"bk_app_code\": \"" + bk_app_code + "\",\n" +
                "\t\"bk_app_secret\": \"" + bk_app_secret + "\",\n" +
                "\t\"bk_username\": \"" + bk_username + "\",\n" +
                "\t\"bk_biz_id\": " + bk_biz_id + ",\n" +
                "\t\"job_instance_id\": " + job_instance_id + "\n" +
                "}";
        return params;

    }

    /**
     * 拼装快速下载文件的接口参数
     * @param downLoadDTO
     * @return
     */
    public String getFastPushFile(DownLoadDTO downLoadDTO) {
        String label = downLoadDTO.getLabel();
        BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
        int bk_biz_id = bkEnum.getCode();
        String ip = downLoadDTO.getIp();
        String path = downLoadDTO.getPath();
        //如果是容器内的日志需要重新拼装
        if (path.contains("/tsf_default/")) {
            String tmp = path.split("/")[4];
            String[] patharr = path.split("-");
            path = "/log/" + patharr[1] + "-" + patharr[2] + "-" + patharr[3] + "-" + patharr[4] + "/" + tmp;
        }
        //准备截取路径，去除文件名，只保留路径
        int end = path.lastIndexOf("/");
        String params = "{\n" +
                "\t\"bk_app_code\": \"" + bk_app_code + "\",\n" +
                "\t\"bk_app_secret\": \"" + bk_app_secret + "\",\n" +
                "\t\"bk_username\": \"" + bk_username + "\",\n" +
                "\t\"bk_biz_id\": " + bk_biz_id + ",\n" +
                "\t\"file_target_path\": \"/tmp/0_" + downLoadDTO.getCvmip() + "/" + path.substring(0, end) + "\",\n" +
                "\t\"account\": \"root\",\n" +
                "\t\"ip_list\": [{\"bk_cloud_id\": 0,\"ip\": \"10.155.27.48\"}],\n" +
                "\t\"file_source\": [{\n" +
                "\t\t\"files\":[\"" + path + "\"],\n" +
                "\t\t\"account\": \"ubuntu\",\n" +
                "\t\t\"ip_list\": [\n";
        String iplist = "\t\t{\n" +
                "\t\t\t\"bk_cloud_id\": 0,\n" +
                "\t\t\t\"ip\": \"" + ip + "\"\n" +
                "\t\t}\n";

        params += iplist;
        params += "\t\t]\n\t}\n]}";
        return params;
    }

    /**
     * @param path
     * @param response
     */
    public void download(String ip, String path, HttpServletResponse response) throws IOException {
        if (path.contains("/tsf_default/")){
            String[] temp = path.split("-");
            path = path.replace("/data/tsf_default/logs","/log/" + temp[1] + "-" + temp[2] + "-" + temp[3] + "-" + temp[4]);
        }
        path = "/tmp" + File.separator + "0_" + ip + File.separator + path;
        // path是指欲下载的文件的路径。
        File file = new File(path);
        if (!file.exists()) {
            log.error("文件不存在");
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
     * 获取文件是否合理存在于文件服务器
     *
     * @param list
     * @return
     */
    public List<LogDetailVO> getFileIsExists(List<LogDetailVO> list) {
        List<LogDetailVO> logs = new ArrayList<>();
        String path;
        //Calendar cal = Calendar.getInstance();
        for (LogDetailVO log : list) {
            if (log.getPath().contains("/data/tsf_default/") && !log.getPath().contains("sys_log.log")){
                System.out.println(log.getPath());
                String[] patharr = log.getPath().split("-");
                path = "/tmp" + File.separator + "0_" + log.getIp() + File.separator + log.getPath().replace("/data/tsf_default/logs", "/log/" + patharr[1] + "-" + patharr[2] + "-" + patharr[3] + "-" + patharr[4]);
            } else {
                path = "/tmp" + File.separator + "0_" + log.getIp() + File.separator + log.getPath();
            }
            File file = new File(path);
            if (!file.exists()) {
                log.setMirror(false);
            } else {
                //最后修改时间
                String mtime = executeLinuxCmd(path);
                //long time = file.lastModified();
                //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                //cal.setTimeInMillis(time);
                //String modifiedTime = formatter.format(cal.getTime());
                if (isToday(log.getCreateTime(), mtime)) {
                    log.setMirror(true);
                } else {
                    file.delete();
                    log.setMirror(false);
                }
            }
            logs.add(log);
        }
        return logs;
    }

    /**
     * 判断文件在文件服务的时间是否合法
     *
     * @param creatTime 文件在蓝鲸的创建时间
     * @param time      文件在文件服务器的最后修改时间
     * @return
     */
    public Boolean isToday(String creatTime, String time) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(creatTime, dtf);
        LocalDateTime modifiedTime = LocalDateTime.parse(time, dtf);
        LocalDateTime now = LocalDateTime.now();
        //如果是蓝鲸创建时间是今天，判断文件在服务器上创建时间和当前时间是否间隔两个小时
        if (now.getYear() == localDateTime.getYear() && now.getMonth() == localDateTime.getMonth()
                && now.getDayOfMonth() == localDateTime.getDayOfMonth()) {
            //获取两个时间的时间间隔
            Duration duration = Duration.between(modifiedTime, now);
            return duration.toMinutes() <= TWOHOURS;
        } else {
            return true;
        }
    }

    /**
     * 拼接容器脚本参数
     * @param downLoadDTO
     * @param fastExecuteScript_id
     * @return
     */
    public String getContainerScriptParams(DownLoadDTO downLoadDTO, int fastExecuteScript_id) {
        String label = downLoadDTO.getLabel();
        String ip = downLoadDTO.getIp();
        BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
        int bk_biz_id = bkEnum.getCode();
        String[] arr = downLoadDTO.getPath().split("/");
        String path = arr[arr.length - 1];
        String[] paths = path.split("-");
        String namespace = paths[1] + "-" + paths[3];
        String group = paths[1] + "-" + paths[2] + "-" +paths[3] + "-" +paths[4];
        String flag = group + "-" + paths[5] + "-" + paths[6];
        String param = namespace + " " + group + " " + flag + " " + path;
        byte[] content = param.getBytes();
        String script_param = encoder.encodeToString(content);
        String params = "{\n" +
                "\t\"bk_app_code\": \"" + bk_app_code + "\",\n" +
                "\t\"bk_app_secret\": \"" + bk_app_secret + "\",\n" +
                "\t\"bk_username\": \"" + bk_username + "\",\n" +
                "\t\"bk_biz_id\": " + bk_biz_id + ",\n" +
                "\t\"script_id\": " + fastExecuteScript_id + ",\n" +
                "\t\"script_param\": \"" + script_param + "\",\n" +
                "\t\"script_timeout\": 1000,\n" +
                "\t\"account\": \"ubuntu\",\n" +
                "\t\"is_param_sensitive\": 0,\n" +
                "\t\"ip_list\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"bk_cloud_id\":0,\n" +
                "\t\t\t\"ip\":\""+ ip +"\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        return params;
    }

    /**
     * 作业是否执行结束
     * @param label
     * @param jobInstanceId
     * @param timeOut
     * @return
     */
    public Boolean isFinish(String label, int jobInstanceId, Long timeOut) {
        String paramsLog = getJobInstanceLogParams(label, jobInstanceId);

        JSONObject resultLog = new JSONObject();
        long t1 = System.currentTimeMillis();
        boolean isFinished = false;
        while (!isFinished) {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            resultLog = callLanJingInterface("http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/", paramsLog);
            isFinished = resultLog.getJSONArray(JsonWordEnum.data.getJsonWord()).getJSONObject(0).getBoolean(JsonWordEnum.is_finished.getJsonWord());

            long t2 = System.currentTimeMillis();
            if (t2 - t1 > timeOut) {
                return isFinished;
            }
        }
        return isFinished;
    }

    /**
     * 获取文件在服务器上的生成时间
     * @param path
     * @return
     */
    public String executeLinuxCmd(String path) {
        String cmd = "stat " + path + " | grep Modify";
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec(new String[] {"/bin/sh", "-c", cmd});
            InputStream in = process.getInputStream();
            BufferedReader bs = new BufferedReader(new InputStreamReader(in));
            List<String> list = new ArrayList<String>();
            String result = null;
            while ((result = bs.readLine()) != null) {
                System.out.println("job result [" + result + "]");
                list.add(result);
            }
            in.close();
            process.destroy();

            return list.get(0).substring(0, list.get(0).lastIndexOf(":")).replace("Modify: ", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
