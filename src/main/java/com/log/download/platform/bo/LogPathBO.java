package com.log.download.platform.bo;

import com.log.download.platform.vo.LogDetailVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-04-13 16:14
 **/
@Data
@Builder
public class LogPathBO {

    //todo 增加接收脚本参数

    /**
     * 日志路径集合
     */
    List<LogDetailVO> list;

    /**
     * 未成功日志ip集合
     */
    String notFinish;
}
