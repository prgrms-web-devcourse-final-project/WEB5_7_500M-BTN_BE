package shop.matjalalzz.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TraceLoggingAspect {

    @Around("execution(* shop.matjalalzz..*Service.*(..))")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        log.info(">> Start: {}", method);

        try {
            Object result = joinPoint.proceed();
            log.info("<< End: {}", method);
            return result;
        } catch (Throwable t) {
            log.error("!! Exception in {}: {}", method, t.getMessage());
            throw t;
        }
    }
}