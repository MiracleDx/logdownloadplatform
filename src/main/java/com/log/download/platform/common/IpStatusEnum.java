package com.log.download.platform.common;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IpStatusEnum
 * 主机任务状态码
 * @author YaoDF
 * Description:
 * Created in: 2020-03-19 09:20
 * Modified by:
 */
@Getter
@AllArgsConstructor
public enum IpStatusEnum {

    /**
     * 主机任务状态码
     */
    A1(1, "Agent异常"),
    A3(3, "上次已成功"),
    A5(5, "等待执行"),
    A7(7, "正在执行"),
    A9(9, "执行成功"),
    A11(11, "任务失败"),
    A12(12, "任务下发失败"),
    A13(13, "任务超时"),
    A15(15, "任务日志错误"),
    A101(101, "脚本执行失败"),
    A102(102, "脚本执行超时"),
    A103(103, "脚本执行被终止"),
    A104(104, "脚本返回码非零"),
    A202(202, "文件传输失败"),
    A203(203, "源文件不存在"),
    A310(310, "Agent异常"),
    A311(311, "用户名不存在"),
    A320(320, "文件获取失败"),
    A321(321, "文件超出限制"),
    A329(329, "文件传输错误"),
    A399(399, "任务执行出错");

    private Integer code;

    private String status;
}
