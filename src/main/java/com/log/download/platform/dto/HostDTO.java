package com.log.download.platform.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * HostDTO
 * 主机任务状态码
 * @author YaoDF
 * Description:
 * Created in: 2020-03-19 09:20
 * Modified by:
 */
@Data
public class HostDTO {
    private List<Map<String,Object>> ip_list;
}
