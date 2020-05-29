package com.log.download.platform.aop;

import com.log.download.platform.support.FileCheckExecutors;
import com.log.download.platform.util.ElasticSearchUtil;
import com.log.download.platform.util.IpUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yaodongfang
 * @program: platform
 * @description:
 * @user: YaoDF
 * @date: 2020-05-26 09:36
 **/
@Aspect
@Component
public class SysLogAspect {

    private static final String UNKNOWN = "unKnown";

    @Resource
    private Environment environment;

    /**
     * 定义切点 @Pointcut
     * 在注解的位置切入代码
     */
    @Pointcut("@annotation(com.log.download.platform.aop.OperatingLog)")
    public void logPointCut() {}

    @Before("logPointCut()")
    public void getSyslog(JoinPoint joinPoint) {

        //从切面植入点处通过反射机制获取植入点处的方法
         MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        OperatingLog operatingLog = method.getAnnotation(OperatingLog.class);
//        String className = joinPoint.getTarget().getClass().getName();
//        String methodName = method.getName();
//        Object[] args = joinPoint.getArgs();
//        String params = JSON.toJSONString(args);
        String userIP = IpUtil.getRealIp(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest());
        FileCheckExecutors fileCheck = FileCheckExecutors.getInstance();
        ElasticSearchUtil es = ElasticSearchUtil.getInstance();
        Map<String, Object> jsonMap = new HashMap<String,Object>(){{
            put("operattime",new Date());
            put("userip", userIP);
            put("operatstate",operatingLog.value());
        }};
        es.init(environment, new String[]{"10.155.208.144:9200"});
        fileCheck.execute(() -> {
            es.creatIndex("operatinglog",jsonMap);
            System.out.println("创建索引成功");
        });
    }

}
