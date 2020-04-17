package com.log.download.platform.controller;

import com.log.download.platform.bo.LogPathBO;
import com.log.download.platform.dto.FindMirrorDTO;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.exception.DataNotFoundException;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.service.LogService;
import com.log.download.platform.vo.LogDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
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
@Slf4j
@RestController
public class LogController {

    @Resource
    private LogService logService;

    /**
     * 查询对应部署组下的日志清单
     *
     * @return
     */
    @PostMapping("/queryLogDetails")
    public ServerResponse<List<LogDetailVO>> queryLogDetails(@RequestBody QueryLogDetailDTO queryLogDetailDTO) {
        LogPathBO logPathBO = logService.queryLogDetails(queryLogDetailDTO);
        if (logPathBO.getNotFinish().isEmpty() && logPathBO.getList().size() != 0) {
            return ServerResponse.success(logPathBO.getList());
        } else if (logPathBO.getList().size() == 0) {
            throw new DataNotFoundException("蓝鲸查询无日志文件列表返回");
        } else {
            return ServerResponse.failure(HttpStatus.SC_NOT_FOUND, logPathBO.getNotFinish(), logPathBO.getList());
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
        if (logService.findFile(findMirrorDTO.getPath(), findMirrorDTO.getIp())) {
            return ServerResponse.success();
        }
        log.error("通过路径获取文件失败，请检查文件是否存在");
        throw new DataNotFoundException("查找镜像文件失败");
    }
}
