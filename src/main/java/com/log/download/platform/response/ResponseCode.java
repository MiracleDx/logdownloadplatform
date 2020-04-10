package com.log.download.platform.response;


import org.apache.http.HttpStatus;

/**
 * @author Dongx
 */
public enum ResponseCode {

    /* 成功状态码 200 */
    SUCCESS(HttpStatus.SC_OK, "成功"),

    /* 参数错误：400 Bad Request */
    PARAM_IS_INVALID(HttpStatus.SC_BAD_REQUEST, "参数无效"),
    PARAM_IS_BLANK(HttpStatus.SC_BAD_REQUEST, "参数为空"),
    PARAM_TYPE_BIND_ERROR(HttpStatus.SC_BAD_REQUEST, "参数类型错误"),
    PARAM_NOT_COMPLETE(HttpStatus.SC_BAD_REQUEST, "参数缺失"),
    PARAM_IS_ALL_BLANK(HttpStatus.SC_BAD_REQUEST,"参数不可同时为空"),

    /* 用户错误：401 Unauthorized */
    USER_NOT_LOGGED_IN(HttpStatus.SC_UNAUTHORIZED, "用户未登录"),
    USER_LOGIN_ERROR(HttpStatus.SC_UNAUTHORIZED, "账号不存在或密码错误"),
    USER_ACCOUNT_FORBIDDEN(HttpStatus.SC_UNAUTHORIZED, "账号已被禁用"),
    USER_NOT_EXIST(HttpStatus.SC_UNAUTHORIZED, "用户不存在"),
    USER_HAS_EXISTED(HttpStatus.SC_UNAUTHORIZED, "用户已存在"),
    USER_NOT_ROLES(HttpStatus.SC_UNAUTHORIZED, "用户无任何权限"),
    USER_PASSWORD_ERROR(HttpStatus.SC_UNAUTHORIZED, "密码错误"),
    USER_UPDATE_ERROR(HttpStatus.SC_UNAUTHORIZED, "头像未指定"),

    /* 权限错误：403 Forbidden */
    PERMISSION_NO_ACCESS(HttpStatus.SC_FORBIDDEN, "无访问权限"),
    
    /* 数据错误：404 Not Found */
    DATA_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "数据未找到"),
    DATA_IS_WRONG(HttpStatus.SC_NOT_FOUND, "数据有误"),
    PARTIAL_DATA_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "部分数据未找到"),
    MENU_EXCEL_NOT_FOUND(HttpStatus.SC_NOT_FOUND,"菜单加载失败，请联系部署组，上传正确的日志清单文件"),

    /* 请求超时：408 Request Timeout */
    REQUEST_TIMEOUT(HttpStatus.SC_REQUEST_TIMEOUT, "请求超时"),
    
    /* 数据冲突：409 Conflict */
    DATA_ALREADY_EXISTED(HttpStatus.SC_CONFLICT, "数据已存在"),

    /* 业务错误：500 Internal Server Error */
    SPECIFIED_QUESTIONED_USER_NOT_EXIST(HttpStatus.SC_INTERNAL_SERVER_ERROR, "某业务出现问题"),

    /* 系统错误：500 Internal Server Error */
    SYSTEM_INNER_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "系统繁忙，请稍后重试")
    
    ;
    
    private Integer code;

    private String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

    public static String getMessage(String name) {
        for (ResponseCode item : ResponseCode.values()) {
            if (item.name().equals(name)) {
                return item.message;
            }
        }
        return name;
    }

    public static Integer getCode(String name) {
        for (ResponseCode item : ResponseCode.values()) {
            if (item.name().equals(name)) {
                return item.code;
            }
        }
        return null;
    }

}
