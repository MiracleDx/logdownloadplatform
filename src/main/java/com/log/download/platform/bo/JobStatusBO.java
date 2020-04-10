package com.log.download.platform.bo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * JobStatusBO
 *
 * @author YaoDF
 * Description:
 * Created in: 2020-04-10 14:57
 * Modified by:
 */
@Data
public class JobStatusBO {

    private JSONObject result;

    private Boolean isFinished;
}
