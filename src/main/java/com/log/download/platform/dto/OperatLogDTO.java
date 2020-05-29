package com.log.download.platform.dto;

import com.log.download.platform.aop.OperatingLog;
import com.log.download.platform.vo.OperatLogVO;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-05-28 16:41
 **/
@Data
@Builder
public class OperatLogDTO {

    /**
     * 操作日志集合
     */
    List<OperatLogVO> operatLogVOS;

    /**
     * 操作日志统计
     */
    HashMap<String, Integer> operats;
}
