package com.log.download.platform.config;

import com.log.download.platform.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author Dongx
 */
@Slf4j
@Aspect
@Component
public class LogAspectConfig {
    
    private final String LOCATION = "service";

    @Pointcut("execution(public * com.log..controller..*.*(..))")
    public void webLog() {}

    @Pointcut("execution(public * com.log..service..*.*(..))")
    public void serviceLog() {}

    @Around(value = "webLog() || serviceLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        String name = target.getClass().getSimpleName().toUpperCase();
        // 切点位置标志位
        boolean flag = false;
        if (name.contains(LOCATION.toUpperCase())) {
            flag = true;
        }
        
        long startTime = System.nanoTime();
        Object result = null;
        
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }

        // 记录下请求内容
        String requestMethod = joinPoint.getSignature().getDeclaringTypeName()+  "." + joinPoint.getSignature().getName();
        String requestArgs = Arrays.toString(joinPoint.getArgs());

        // 方法执行
        result = joinPoint.proceed();

        // 获取响应码 由于在AOP中永远在方法执行完毕后才执行 所以状态码永远是200
        //HttpServletResponse httpServletResponse = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //int responseStatus = httpServletResponse.getStatus();

        long endTime = System.nanoTime();
        
        // 如果不是http请求进入，则没有对应的http属性
        if (request == null) {
            log.info("AOP_TYPE: [{}], URL: [{}], HTTP_METHOD: [{}], REQUEST_IP: [{}], REQUEST_METHOD: [{}], REQUEST_ARGS: [{}], RESPONSE_ARGS: [{}], RESPONSE_TIME: [{}]",
                    flag ? "service" : "controller", "-", "-", "-", requestMethod, requestArgs, result,
                    (endTime - startTime) / 1000_000);
        } else {
            log.info("AOP_TYPE: [{}], URL: [{}], HTTP_METHOD: [{}], REQUEST_IP: [{}], REQUEST_METHOD: [{}], REQUEST_ARGS: [{}], RESPONSE_ARGS: [{}], RESPONSE_TIME: [{}]",
                    flag ? "service" : "controller", request.getRequestURI(), request.getMethod(), IpUtil.getRealIp(request), requestMethod, requestArgs, result,
                    (endTime - startTime) / 1000_000);
        }
       
        return result;
    }
}
