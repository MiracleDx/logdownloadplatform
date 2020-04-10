package com.log.download.platform.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.slf4j.MDC;

import java.time.LocalDateTime;

/**
 * 服务响应 
 * @author dongx
 * @param <T>
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ServerResponse<T> implements IResult {

    private static final long serialVersionUID = 1L;
    
    private static final String REQUEST_ID = "requestId";
    
    private Integer code;

    private String message;

    private T data;
    
    private String requestId;
    
    private LocalDateTime occurredTime;
    
    private ServerResponse(){
        
    }
    
    private ServerResponse(Integer code)  {
        this.code = code;
    }

    private ServerResponse(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    private ServerResponse(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    private ServerResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private ServerResponse(Integer code, String message, T data, String requestId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
    }

    private ServerResponse(Integer code, String message, String requestId, LocalDateTime localDateTime) {
        this.code = code;
        this.message = message;
        this.requestId = requestId;
        this.occurredTime = localDateTime;
    }

    private ServerResponse(Integer code, String message, T data, String requestId, LocalDateTime localDateTime) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
        this.occurredTime = localDateTime;
    }

    public static <T> ServerResponse<T> success() {
        return new ServerResponse(ResponseCode.SUCCESS.code(), ResponseCode.SUCCESS.message(), null, MDC.get(REQUEST_ID));
    }

    public static <T> ServerResponse<T> success(Integer code) {
        return new ServerResponse<>(code, null, null, MDC.get(REQUEST_ID));
    }

    public static <T> ServerResponse<T> success(String message) {
        return new ServerResponse<>(ResponseCode.SUCCESS.code(), message, null, MDC.get(REQUEST_ID));
    }

    public static <T> ServerResponse<T> success(T data) {
        return new ServerResponse<>(ResponseCode.SUCCESS.code(), ResponseCode.SUCCESS.message(), data, MDC.get(REQUEST_ID));
    }

    public static <T> ServerResponse<T> success(String message, T data) {
        return new ServerResponse<>(ResponseCode.SUCCESS.code(), message, data, MDC.get(REQUEST_ID));
    }

    public static <T> ServerResponse<T> failure(Integer code) {
        return new ServerResponse<>(code, null, null, MDC.get(REQUEST_ID), LocalDateTime.now());
    }

    public static <T> ServerResponse<T> failure(String message) {
        return new ServerResponse<>(ResponseCode.PARAM_IS_INVALID.code(), message, null, MDC.get(REQUEST_ID), LocalDateTime.now());
    }

    public static <T> ServerResponse<T> failure(Integer code, String message) {
        return new ServerResponse<>(code, message, MDC.get(REQUEST_ID), LocalDateTime.now());
    }

    public static <T> ServerResponse<T> failure(String message, T data) {
        return new ServerResponse<>(ResponseCode.SYSTEM_INNER_ERROR.code(), message, data, MDC.get(REQUEST_ID), LocalDateTime.now());
    }

    public static <T> ServerResponse<T> failure(Integer code, String message, T data) {
        return new ServerResponse<>(code, message, data, MDC.get(REQUEST_ID), LocalDateTime.now());
    }

}
