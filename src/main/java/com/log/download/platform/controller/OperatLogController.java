package com.log.download.platform.controller;

import com.log.download.platform.dto.OperatLogDTO;
import com.log.download.platform.service.OperatLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-05-28 09:34
 **/
@Slf4j
@RestController
public class OperatLogController {

    @Resource
    private OperatLogService operatLogService;

    @GetMapping("/test")
    public OperatLogDTO getOperatingLog(long startDate, long endDate) {
        int startLen = (new Long(startDate)).toString().length();
        int endLen = (new Long(endDate)).toString().length();
        if (startLen == 13 && endLen == startLen) {
            return operatLogService.getOperatingLog(startDate, endDate);
        }
        throw new NullPointerException("时间格式错误");
    }
}


