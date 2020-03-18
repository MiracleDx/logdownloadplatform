package com.log.download.platform.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class HostDTO {
    private List<Map<String,Object>> ip_list;
}
