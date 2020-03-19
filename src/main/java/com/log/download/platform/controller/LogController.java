package com.log.download.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.service.CallBKInterfaceService;
import com.log.download.platform.vo.LogDetailVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
@RestController
public class LogController {

    @Resource
    private CallBKInterfaceService callBKInterfaceService;

    //查询对应部署组下的日志清单
    @Value("${scripid1}")
    private int script_id;

    /**
     * 查询对应部署组下的日志清单
     *
     * @return
     */
//    @PostMapping("/queryLogDetails")
//    public ServerResponse<List<LogDetailVO>> queryLogDetails(@RequestBody QueryLogDetailDTO queryLogDetailDTO) {
//        String[] paramArr = null;
//        //拼接快速执行脚本的参数
//        String params = callBKInterfaceService.getFastExecuteScriptParams(queryLogDetailDTO.getLabel(), queryLogDetailDTO.getIps(), script_id, paramArr);
//        //执行脚本，并获取结果
//        JSONObject result = callBKInterfaceService.callLanJingInterface("", params);
//        //如果执行成功，查询执行日志
//        if (Boolean.valueOf(result.getJSONObject("result").toString())) {
//            //获取作业实例id
//            int job_instance_id = result.getJSONObject("data").getInteger("job_instance_id");
//            //查询日志
//            String params_log = callBKInterfaceService.getJobInstanceLogParams(queryLogDetailDTO.getLabel(), job_instance_id);
//            JSONObject result_log = callBKInterfaceService.callLanJingInterface("", params_log);
//            //获取log日志
//            String log_content = result_log.getJSONArray("data").getJSONObject(0).getJSONArray("step_results").getJSONObject(0).getJSONArray("ip_logs").getJSONObject(0).getJSONObject("log_content").toString();
//            //处理log为路径
//            String[] paths = log_content.split("\\n");
//            LogDetailVO logDetail = new LogDetailVO();
//            List<LogDetailVO> list = null;
//            for (int i = 1; i <= paths.length; i++) {
//                logDetail.setId(i);
//                logDetail.setPath(paths[i - 1]);
//                list.add(logDetail);
//            }
//            return ServerResponse.success(list);
//        }
//        return ServerResponse.failure("执行脚本失败");
//    }

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
            JSONObject result_log = callBKInterfaceService.callLanJingInterface("http:// paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/", params_log);
            if (result_log.getBoolean("result")) {
                String log_content = result_log.getJSONArray("data").getJSONObject(0).getJSONArray("step_results").getJSONObject(0).getJSONArray("ip_logs").getJSONObject(0).getJSONObject("log_content").toString();
                //处理log为路径
                String[] paths = log_content.split("\\n");
                LogDetailVO logDetail = new LogDetailVO();
                List<LogDetailVO> list = null;
                for (int i = 1; i <= paths.length; i++) {
                    logDetail.setId(i);
                    logDetail.setPath(paths[i - 1]);
                    list.add(logDetail);
                }
                return ServerResponse.success(list);
            }
        }
        return ServerResponse.failure("执行脚本失败");
    }
}
