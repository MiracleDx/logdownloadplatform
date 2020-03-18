package com.log.download.platform.dto;

import lombok.Data;

import java.util.ArrayList;

@Data
public class DownLoadDTO {

    /**
     * 服务编号
     */
    private String label;

    /**
     * ip集合
     */
    private String[] ips;
}
