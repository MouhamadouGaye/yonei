
package com.mgaye.yonei.aspect;

import com.mgaye.yonei.annotation.Auditable;
import com.mgaye.yonei.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Around("@annotation(com.mgaye.moneytransfer.annotation.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.mgaye.yonei.annotation.Auditable auditable = method
                .getAnnotation(com.mgaye.yonei.annotation.Auditable.class);

        Long userId = getCurrentUserId();
        String methodName = method.getName();
        String action = auditable.action();
        String description = auditable.description().isEmpty() ? "Method executed: " + methodName
                : auditable.description();

        try {
            Object result = joinPoint.proceed();

            // Log successful execution
            auditService.logEvent(userId, action, description);

            return result;

        } catch (Exception e) {
            // Log failed execution
            auditService.logFailedAttempt(userId, action + "_FAILED",
                    "Method failed: " + methodName + " - " + e.getMessage(),
                    getClientIp(), getUserAgent());
            throw e;
        }
    }

    private Long getCurrentUserId() {
        // TODO: Implement based on your security context
        // Example: Extract from SecurityContext
        // Authentication authentication =
        // SecurityContextHolder.getContext().getAuthentication();
        // if (authentication != null && authentication.getPrincipal() instanceof
        // UserDetails) {
        // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // return userService.findByEmail(userDetails.getUsername()).getId();
        // }
        return 1L; // Placeholder - implement properly
    }

    private String getClientIp() {
        // TODO: Implement IP extraction from HttpServletRequest
        // You can use RequestContextHolder to get current request
        return "unknown";
    }

    private String getUserAgent() {
        // TODO: Implement user agent extraction
        return "unknown";
    }
}

// @Aspect
// @Component
// public class AuditAspect {

// private final AuditService auditService;
// private final Auditable auditable;

// public AuditAspect(AuditService auditService, Auditable auditable) {
// this.auditService = auditService;
// this.auditable = auditable;
// }

// @Around("@annotation(auditable)")
// public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable)
// throws Throwable {
// Long userId = getCurrentUserId();
// String methodName = joinPoint.getSignature().getName();

// try {
// Object result = joinPoint.proceed();

// // Log successful execution
// auditService.logEvent(userId, auditable.action(),
// auditable.description().isEmpty() ? "Method executed: " + methodName :
// auditable.description());

// return result;

// } catch (Exception e) {
// // Log failed execution
// auditService.logFailedAttempt(userId, auditable.action() + "_FAILED",
// "Method failed: " + methodName + " - " + e.getMessage(),
// getClientIp(), getUserAgent());
// throw e;
// }
// }

// private Long getCurrentUserId() {
// // Implement based on your security context
// return 1L; // Placeholder
// }

// private String getClientIp() {
// // Implement IP extraction
// return "unknown";
// }

// private String getUserAgent() {
// // Implement user agent extraction
// return "unknown";
// }
// }