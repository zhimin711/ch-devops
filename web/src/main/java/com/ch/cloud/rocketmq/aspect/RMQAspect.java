package com.ch.cloud.rocketmq.aspect;

import com.ch.cloud.devops.utils.RequestUtil;
import com.ch.cloud.rocketmq.service.OpsService;
import com.ch.cloud.rocketmq.util.RMQAdminUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/16
 */
@Aspect
@Service
@Slf4j
public class RMQAspect {
    
    
    @Resource
    private OpsService opsService;
    
    @Pointcut("execution(* com.ch.cloud.rocketmq.manager.*..*(..))")
    public void mqAdminMethodPointCut() {
    
    }
    
    @Around(value = "mqAdminMethodPointCut()")
    public Object aroundMQAdminMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object obj = null;  // 获取当前请求
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
                RequestContextHolder.getRequestAttributes())).getRequest();
        
        // 从 Cookie 中获取 nameSrvAddr
        String nameSrvAddr = RequestUtil.getFromCookie(request, "nameSrvAddr");
        
        try {
            RMQAdminUtil.initMQAdminExt(opsService.getClient(nameSrvAddr).getAddr());
            obj = joinPoint.proceed();
        } finally {
            RMQAdminUtil.destroyMQAdminExt();
            log.debug("op=look adrr={} method={} cost={}", nameSrvAddr, joinPoint.getSignature().getName(),
                    System.currentTimeMillis() - start);
        }
        return obj;
    }
    
}
