package com.log.download.platform.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author Dongx
 */
@Slf4j
@Aspect
@Component
public class LogServiceAspectConfig {
    
    @Pointcut("execution(public * com.log..service..*.*(..))")
    public void serviceLog() {}
    
    @Order(3)
    @Around(value = "serviceLog()")
    public Object aroundService(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.nanoTime();
        Object result = null;

        // 记录下请求内容
        String requestMethod = joinPoint.getSignature().getDeclaringTypeName()+  "." + joinPoint.getSignature().getName();
        String requestArgs = Arrays.toString(joinPoint.getArgs());

        // 方法执行
        result = joinPoint.proceed();

        long endTime = System.nanoTime();

        log.info("AOP_TYPE: [{}], URL: [{}], HTTP_METHOD: [{}], REQUEST_IP: [{}], REQUEST_METHOD: [{}], REQUEST_ARGS: [{}], RESPONSE_ARGS: [{}], RESPONSE_TIME: [{}]",
                "service", "-", "-", "-", requestMethod, requestArgs, result,
                (endTime - startTime) / 1000_000);
       
        return result;
    }
}
