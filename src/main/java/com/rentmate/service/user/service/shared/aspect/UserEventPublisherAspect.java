package com.rentmate.service.user.service.shared.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j @Aspect @Component
public class UserEventPublisherAspect {

    // Pointcut — matches any method in UserEventPublisherImpl
    @Pointcut("execution(* com.rentmate.service.user.service.implementation.UserEventPublisherImpl.*(..))")
    public void userEventPublisherMethods() {}

    /**
     * Around advice — wraps method execution, logs and handles exceptions
     */
    @Around("userEventPublisherMethods()")
    public Object handleAndLogExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        try {
            return joinPoint.proceed();
        } catch (Exception ex) {
            log.error("Exception in {} with message: {}", methodName, ex.getMessage(), ex);
            return null;
        }
    }

    // This advice can't prevent exceptions from being propagated
//    @AfterThrowing(pointcut = "userEventPublisherMethods()", throwing = "ex")
//    public void logException(JoinPoint joinPoint, Exception ex) {
//        String methodName = joinPoint.getSignature().toShortString();
//        log.error("Exception in {} with message: {}", methodName, ex.getMessage(), ex);
//    }
}
