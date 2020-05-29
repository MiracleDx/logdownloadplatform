package com.log.download.platform.aop;

import java.lang.annotation.*;

/**
 * @author Yaodongfang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperatingLog {
    String value() default "";
}
