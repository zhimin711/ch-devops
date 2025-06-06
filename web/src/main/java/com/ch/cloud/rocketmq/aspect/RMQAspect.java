package com.ch.cloud.rocketmq.aspect;

import com.ch.cloud.rocketmq.util.RMQAdminUtil;
import com.ch.cloud.rocketmq.config.RMQConfigure;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

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
    private RMQConfigure configure;
    
    @Pointcut("execution(* com.ch.cloud.rocketmq.manager.*..*(..))")
    public void mqAdminMethodPointCut() {
    
    }
    
    @Around(value = "mqAdminMethodPointCut()")
    public Object aroundMQAdminMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object obj = null;  // 获取当前请求
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
                RequestContextHolder.getRequestAttributes())).getRequest();
        
        // 从 Cookie 中获取 nameSvrAddr
        String nameSvrAddr = getNameSvrAddrFromCookie(request);
        
        
        try {
            RMQAdminUtil.initMQAdminExt(getClient(nameSvrAddr));
            obj = joinPoint.proceed();
        } finally {
            RMQAdminUtil.destroyMQAdminExt();
            log.debug("op=look method={} cost={}", joinPoint.getSignature().getName(),
                    System.currentTimeMillis() - start);
        }
        return obj;
    }
    
    
    public String getClient(String nameSvrAddr) {
        Optional<RMQConfigure.Client> first = configure.getClients().stream()
                .filter(client -> client.getAddr().equals(nameSvrAddr)).findFirst();
        if (first.isPresent()) {
            return first.get().getAddr();
        }
        return configure.getClients().get(0).getAddr();
    }
    
    // 提取 Cookie 的辅助方法
    private String getNameSvrAddrFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("nameSvrAddr".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
