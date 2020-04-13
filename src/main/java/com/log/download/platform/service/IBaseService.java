package com.log.download.platform.service;

import com.log.download.platform.dto.DownLoadDTO;

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

    /**
     * 调用蓝鲸快速分发接口
     * @param downLoadDTO
     */
    void fastPushFile(DownLoadDTO downLoadDTO);
}
