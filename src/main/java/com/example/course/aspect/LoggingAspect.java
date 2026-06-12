package com.example.course.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // Ghi nhật ký thời gian thực thi cho TẤT CẢ các phương thức trong lớp dịch vụ
    @Around("execution(* com.example.course.service.impl.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String method = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[PERF] {} completed in {}ms", method, elapsed);
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[PERF] {} failed after {}ms with error: {}", method, elapsed, ex.getMessage());
            throw ex;
        }
    }

    // Ghi lại các sự kiện chấm điểm thông qua @AfterReturning
    @AfterReturning(
            pointcut = "execution(* com.example.course.service.impl.SubmissionServiceImpl.gradeSubmission(..))",
            returning = "result"
    )
    public void logAfterGrading(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            log.info("[GRADE] Submission graded successfully. Args: {}", Arrays.toString(args));
        }
    }

    // Ghi nhật ký các ngoại lệ về chấm điểm thông qua @AfterThrowing
    @AfterThrowing(
            pointcut = "execution(* com.example.course.service.impl.SubmissionServiceImpl.gradeSubmission(..))",
            throwing = "ex"
    )
    public void logAfterGradingException(JoinPoint joinPoint, Throwable ex) {
        log.error("[GRADE] Grading failed: {}", ex.getMessage());
    }

    // Ghi nhật ký tất cả các lệnh gọi phương thức của bộ điều khiển
    @Before("execution(* com.example.course.controller.*.*(..))")
    public void logControllerRequest(JoinPoint joinPoint) {
        log.debug("[REQUEST] {}.{}() called",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
    }

    // Ghi nhật ký các sự kiện xác thực
    @AfterReturning(
            pointcut = "execution(* com.example.course.service.impl.AuthServiceImpl.login(..))",
            returning = "result"
    )
    public void logSuccessfulLogin(JoinPoint joinPoint, Object result) {
        log.info("[AUTH] User logged in successfully");
    }

    @AfterReturning(
            pointcut = "execution(* com.example.course.service.impl.AuthServiceImpl.logout(..))"
    )
    public void logLogout(JoinPoint joinPoint) {
        log.info("[AUTH] User logged out, token blacklisted");
    }

    // Ghi nhật ký các sự kiện tải lên tập tin
    @AfterReturning(
            pointcut = "execution(* com.example.course.service.impl.CloudinaryServiceImpl.uploadFile(..))",
            returning = "url"
    )
    public void logFileUpload(JoinPoint joinPoint, Object url) {
        log.info("[UPLOAD] File uploaded successfully to: {}", url);
    }
}
