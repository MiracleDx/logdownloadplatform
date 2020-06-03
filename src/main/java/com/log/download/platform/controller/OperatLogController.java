package com.log.download.platform.controller;

import com.log.download.platform.service.OperatLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

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

    @GetMapping("/operate")
    public Map<String, Integer> getOperatingLog(long startDate, long endDate) {

        return operatLogService.getOperatingLog().getOperats();
    }
}


