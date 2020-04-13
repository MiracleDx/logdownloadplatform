package com.log.download.platform.controller;

import com.log.download.platform.bo.LogPathBO;
import com.log.download.platform.dto.FindMirrorDTO;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.response.ResponseCode;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.service.LogPathService;
import com.log.download.platform.vo.LogDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

import static com.log.download.platform.response.ResponseCode.PARTIAL_DATA_NOT_FOUND;

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
    private LogPathService logPathService;

    /**
     * 查询对应部署组下的日志清单
     *
     * @return
     */
    @PostMapping("/queryLogDetails")
    public ServerResponse<List<LogDetailVO>> queryLogDetails(@RequestBody QueryLogDetailDTO queryLogDetailDTO) {
        LogPathBO logPathBO = logPathService.queryLogDetails(queryLogDetailDTO);
        if (logPathBO.getNotFinish().isEmpty() && logPathBO.getList().size() != 0) {
            return ServerResponse.success(logPathBO.getList());
        } else if (logPathBO.getList().size() == 0) {
            return ServerResponse.failure("蓝鲸查询无日志文件列表返回");
        } else {
            return ServerResponse.failure(PARTIAL_DATA_NOT_FOUND.code(), logPathBO.getNotFinish(), logPathBO.getList());
        }
    }

    /**
     * 回调函数，查询镜像是否已经存在
     *
     * @param findMirrorDTO
     * @return
     */
    @PostMapping("/findMirror")
    public ServerResponse<List<LogDetailVO>> findMirror(@RequestBody FindMirrorDTO findMirrorDTO) {
        if (!"".equals(findMirrorDTO.getPath()) && findMirrorDTO.getPath() != null) {
            String path = "";
            if (findMirrorDTO.getPath().contains("/data/tsf_default/")) {
                String[] patharr = findMirrorDTO.getPath().split("-");
                path = "/tmp" + File.separator + "0_" + findMirrorDTO.getIp() + File.separator + findMirrorDTO.getPath().replace("/data/tsf_default/logs", "/log/" + patharr[1] + "-" + patharr[2] + "-" + patharr[3] + "-" + patharr[4]);
            } else {
                path = "/tmp" + File.separator + "0_" + findMirrorDTO.getIp() + File.separator + findMirrorDTO.getPath();
            }
            log.info("查找日志镜像路径: {}", path);
            File file = new File(path);
            if (file.exists()) {
                return ServerResponse.success();
            }
            return ServerResponse.failure(ResponseCode.DATA_NOT_FOUND.code(), ResponseCode.DATA_NOT_FOUND.message());
        }
        return ServerResponse.failure("传送路径失败");
    }
}
