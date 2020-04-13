package com.log.download.platform.exception;


import com.log.download.platform.response.ResponseCode;

/**
 * ParamValidException
 * 参数校验异常
 * @author Dongx
 * Description: 
 * Created in: 2019-01-15 10:34
 * Modified by:
 */
public class ParamValidException extends BusinessException {

    public ParamValidException(ResponseCode responseCode) {
        super(responseCode);
    }

    public ParamValidException(Integer code, String msg) {
        super(code, msg);
    }

    public ParamValidException(ResponseCode responseCode, String msg) {
        super(responseCode, msg);
    }
}
