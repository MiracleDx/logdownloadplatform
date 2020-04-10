package com.log.download.platform.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 服务响应 
 * @author dongx
 * @param <T>
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ServerResponse<T> implements IResult {

    private static final long serialVersionUID = 1L;
    
    private Integer code;

    private String message;

    private T data;
    
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

    public static <T> ServerResponse<T> success() {
        return new ServerResponse(ResponseCode.SUCCESS.code(), ResponseCode.SUCCESS.message());
    }

    public static <T> ServerResponse<T> success(Integer code) {
        return new ServerResponse<>(code);
    }

    public static <T> ServerResponse<T> success(String message) {
        return new ServerResponse<>(ResponseCode.SUCCESS.code(), message);
    }

    public static <T> ServerResponse<T> success(T data) {
        return new ServerResponse<>(ResponseCode.SUCCESS.code(), ResponseCode.SUCCESS.message(), data);
    }

    public static <T> ServerResponse<T> success(String message, T data) {
        return new ServerResponse<>(ResponseCode.SUCCESS.code(), message, data);
    }

    public static <T> ServerResponse<T> failure(Integer code) {
        return new ServerResponse<>(code);
    }

    public static <T> ServerResponse<T> failure(String message) {
        return new ServerResponse<>(ResponseCode.PARAM_IS_INVALID.code(), message);
    }

    public static <T> ServerResponse<T> failure(Integer code, String message) {
        return new ServerResponse<>(code, message);
    }

    public static <T> ServerResponse<T> failure(String message, T data) {
        return new ServerResponse<>(ResponseCode.SYSTEM_INNER_ERROR.code(), message, data);
    }

    public static <T> ServerResponse<T> failure(Integer code, String message, T data) {
        return new ServerResponse<>(code, message, data);
    }

}
