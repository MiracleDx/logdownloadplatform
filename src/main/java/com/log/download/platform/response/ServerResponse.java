package com.log.download.platform.response;

import lombok.Data;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * 服务响应 
 * @author dongx
 * @param <T>
 */
@Data
//@JsonInclude(value = JsonInclude.Include.NON_NULL)
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
        return new ServerResponse<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), null, MDC.get(REQUEST_ID));
    }

    public static <T> ServerResponse<T> success(Integer code) {
        return new ServerResponse<>(code, null, null, MDC.get(REQUEST_ID));
    }

    public static <T> ServerResponse<T> success(T data) {
        return new ServerResponse<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data, MDC.get(REQUEST_ID));
    }

    public static <T> ServerResponse<T> success(String message, T data) {
        return new ServerResponse<>(HttpStatus.OK.value(), message, data, MDC.get(REQUEST_ID));
    }

    public static <T> ServerResponse<T> failure(Integer code, String message) {
        return new ServerResponse<>(code, message, MDC.get(REQUEST_ID), LocalDateTime.now());
    }

    public static <T> ServerResponse<T> failure(Integer code, String message, T data) {
        return new ServerResponse<>(code, message, data, MDC.get(REQUEST_ID), LocalDateTime.now());
    }

}
