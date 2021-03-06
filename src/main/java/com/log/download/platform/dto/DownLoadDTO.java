package com.log.download.platform.dto;

import lombok.Data;

/**
 * DownLoadDTO
 * 主机任务状态码
 * @author YaoDF
 * Description:
 * Created in: 2020-03-19 09:20
 * Modified by:
 */
@Data
public class DownLoadDTO {

    /**
     * 服务编号
     */
    private String label;

    /**
     * ip集合
     */
    private String ip;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 日志分发ip
     */
    private String cvmIp;

    /**
     * 容器名称
     */
    private String hostname;

    /**
     * 蓝鲸需要的参数
     */
    private String bkParam;

}
