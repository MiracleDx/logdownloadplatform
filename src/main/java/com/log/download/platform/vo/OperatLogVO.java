package com.log.download.platform.vo;

import lombok.Data;

/**
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-05-28 16:45
 **/
@Data
public class OperatLogVO {

    /**
     * 操作时间
     */
    String operattime;

    /**
     * 操作用户IP
     */
    String userip;

    /**
     * 操作类型
     */
    String operatstate;
}
