package com.log.download.platform.response;

import java.lang.annotation.*;

/**
 * ResponseResult
 * 接口返回结果注解
 * @author Dongx
 * Description:
 * Created in: 2019-01-14 17:55
 * Modified by:
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseResult {

	Class<? extends IResult> value() default ServerResponse.class;
}
