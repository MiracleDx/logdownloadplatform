package com.log.download.platform.common;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * StatusEnum
 * Json出现的字段名
 * @author YaoDF
 * Description:
 * Created in: 2020-04-07 09:20
 * Modified by:
 */
@Getter
@AllArgsConstructor
public enum JsonWordEnum {

    data("data"),
    result("result"),
    is_finished("is_finished"),
    step_results("step_results"),
    ip_logs("ip_logs"),
    ip_status("ip_status"),
    status("status"),
    message("message"),
    log_content("log_content"),
    tsf_default("/tsf_default/"),
    job_instance_id("job_instance_id");

    private String jsonWord;
}
