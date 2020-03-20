package com.log.download.platform.common;


import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * StatusEnum
 * 作业执行状态
 * @author YaoDF
 * Description:
 * Created in: 2020-03-19 09:20
 * Modified by:
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

    /**
     * 作业执行状态码
     */
    A1(1, "未执行"),
    A2(2, "正在执行"),
    A3(3, "执行成功"),
    A4(4, "执行失败"),
    A5(5, "跳过"),
    A6(6, "忽略错误"),
    A7(7, "等待用户"),
    A8(8, "手动结束"),
    A9(9, "状态异常"),
    A10(10, "步骤强制终止中"),
    A11(11, "步骤强制终止成功"),
    A12(12, "步骤强制终止失败");

    private Integer code;

    private String status;
}
