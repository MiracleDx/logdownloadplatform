package com.log.download.platform.config;

import com.log.download.platform.exception.InternalServerException;
import com.log.download.platform.exception.PermissionForbiddenException;
import com.log.download.platform.support.Resubmit;
import com.log.download.platform.support.ResubmitLock;
import com.log.download.platform.util.IpUtil;
import com.log.download.platform.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.boot.context.properties.source.ConfigurationPropertyState.PRESENT;

/**
 * @author Dongx
 */
@Slf4j
//@Aspect
//@Component
public class LogControllerAspectConfig {
    
    @Pointcut("execution(public * com.log..controller..*.*(..))")
    public void webLog() {}

    @Order(1)
    @Around(value = "webLog()")
    public Object reSubmit(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取调用方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        //获取注解信息
        Resubmit annotation = method.getAnnotation(Resubmit.class);

        if (annotation != null) {
            // 获取调用参数
            Object[] pointArgs = joinPoint.getArgs();

            // 获取注解上的重复调用间隔
            int delaySeconds = annotation.delaySeconds();
            // 获取加锁key
            String key = ResubmitLock.handleKey(JsonUtil.object2Json(pointArgs));
            
            //执行锁
            boolean lock = false;
            try {
                //设置解锁key
                lock = ResubmitLock.getInstance().lock(key, PRESENT);
                
                // 上锁成功
                if (lock) {
                    // 放行
                    return joinPoint.proceed();
                } else {
                    // 响应重复提交异常
                    throw new PermissionForbiddenException("重复请求");
                }
            } finally {
                //设置解锁key和解锁时间
                ResubmitLock.getInstance().unLock(lock, key, delaySeconds);
            }
        } 
        
        return joinPoint.proceed();
    }

    @Order(2)
    @Around(value = "webLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        String name = target.getClass().getSimpleName().toUpperCase();
        
        long startTime = System.nanoTime();
        Object result = null;
        
        // 接收到请求，记录请求内容
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Optional.ofNullable(requestAttributes).orElseThrow(() -> {throw new InternalServerException("");}).getRequest();

                // 记录下请求内容
        String requestMethod = joinPoint.getSignature().getDeclaringTypeName()+  "." + joinPoint.getSignature().getName();
        String requestArgs = Arrays.toString(joinPoint.getArgs());

        // 方法执行
        result = joinPoint.proceed();

        // 获取响应码 由于在AOP中永远在方法执行完毕后才执行 所以状态码永远是200
        //HttpServletResponse httpServletResponse = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //int responseStatus = httpServletResponse.getStatus();

        long endTime = System.nanoTime();
        
        log.info("AOP_TYPE: [{}], URL: [{}], HTTP_METHOD: [{}], REQUEST_IP: [{}], REQUEST_METHOD: [{}], REQUEST_ARGS: [{}], RESPONSE_ARGS: [{}], RESPONSE_TIME: [{}]",
                 "controller", request.getRequestURI(), request.getMethod(), IpUtil.getRealIp(request), requestMethod, requestArgs, result,
                (endTime - startTime) / 1000_000);
       
        return result;
    }
}
