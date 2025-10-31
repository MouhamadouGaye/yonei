package com.mgaye.yonei.interceptor;

import com.mgaye.yonei.service.AuditService;
import com.mgaye.yonei.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditService auditService;
    private final UserService userService;

    public AuditInterceptor(AuditService auditService, UserService userService) {
        this.auditService = auditService;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            // Store request info for postHandle
            request.setAttribute("startTime", System.currentTimeMillis());
            request.setAttribute("handlerMethod", handlerMethod);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {

        if (handler instanceof HandlerMethod) {
            Long startTime = (Long) request.getAttribute("startTime");
            Long duration = System.currentTimeMillis() - startTime;

            // Log slow requests
            if (duration > 5000) { // 5 seconds
                String username = getUsernameFromRequest(request);
                if (username != null) {
                    Long userId = getUserIdFromRequest(username);
                    if (userId != null) {
                        auditService.logEvent(userId, "SLOW_REQUEST",
                                String.format("Request to %s took %d ms", request.getRequestURI(), duration));
                    }
                }
            }

            // Log failed requests
            if (ex != null) {
                String username = getUsernameFromRequest(request);
                if (username != null) {
                    Long userId = getUserIdFromRequest(username);
                    if (userId != null) {
                        auditService.logFailedAttempt(userId, "REQUEST_FAILED",
                                String.format("Request to %s failed: %s", request.getRequestURI(), ex.getMessage()),
                                getClientIp(request), request.getHeader("User-Agent"));
                    }
                }
            }
        }
    }

    private String getUsernameFromRequest(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }

    private Long getUserIdFromRequest(String username) {
        try {
            return userService.findByEmail(username)
                    .map(user -> user.getId())
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}