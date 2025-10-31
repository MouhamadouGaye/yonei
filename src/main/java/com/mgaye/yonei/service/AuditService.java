package com.mgaye.yonei.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mgaye.yonei.entity.AuditLog;
import com.mgaye.yonei.repository.AuditLogRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    // All other methods remain the same...
    public void logEvent(Long userId, String action, String description) {
        AuditLog auditLog = new AuditLog(userId, action, description);
        auditLogRepository.save(auditLog);
    }

    public void logSecurityEvent(Long userId, String action, String description) {
        AuditLog auditLog = new AuditLog(userId, action, description);
        auditLog.setResourceType("SECURITY");
        auditLogRepository.save(auditLog);
    }

    // Profile update with change tracking
    public void logProfileUpdate(Long userId, Map<String, String> changes, String action) {
        try {
            AuditLog auditLog = new AuditLog(userId, action, "User profile updated");
            auditLog.setResourceType("USER");
            auditLog.setResourceId(userId);
            auditLog.setOldValues(objectMapper.writeValueAsString(changes));
            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            // Fallback to simple logging
            logSecurityEvent(userId, action, "Profile updated with changes: " + changes.toString());
        }
    }

    // Financial transaction auditing
    public void logTransactionEvent(Long userId, String action, String description,
            Long transactionId, BigDecimal amount, String currency) {
        AuditLog auditLog = new AuditLog(userId, action, description);
        auditLog.setResourceType("TRANSACTION");
        auditLog.setResourceId(transactionId);

        Map<String, Object> details = new HashMap<>();
        details.put("amount", amount);
        details.put("currency", currency);
        details.put("transactionId", transactionId);

        try {
            auditLog.setNewValues(objectMapper.writeValueAsString(details));
        } catch (JsonProcessingException e) {
            // Ignore JSON errors for audit details
        }

        auditLogRepository.save(auditLog);
    }

    // Payment method auditing
    public void logPaymentMethodEvent(Long userId, String action, String paymentMethodId,
            String cardLast4, String cardBrand) {
        AuditLog auditLog = new AuditLog(userId, action, "Payment method operation: " + action);
        auditLog.setResourceType("PAYMENT_METHOD");

        Map<String, Object> details = new HashMap<>();
        details.put("paymentMethodId", paymentMethodId);
        details.put("cardLast4", cardLast4);
        details.put("cardBrand", cardBrand);

        try {
            auditLog.setNewValues(objectMapper.writeValueAsString(details));
        } catch (JsonProcessingException e) {
            // Ignore JSON errors
        }

        auditLogRepository.save(auditLog);
    }

    // Login/authentication auditing
    public void logAuthenticationEvent(Long userId, String action, String status,
            String ipAddress, String userAgent) {
        AuditLog auditLog = new AuditLog(userId, action, "Authentication attempt: " + status);
        auditLog.setResourceType("AUTHENTICATION");
        auditLog.setStatus(status);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLogRepository.save(auditLog);
    }

    // Failed attempt logging for security monitoring
    public void logFailedAttempt(Long userId, String action, String description,
            String ipAddress, String userAgent) {
        AuditLog auditLog = new AuditLog(userId, action, description);
        auditLog.setResourceType("SECURITY");
        auditLog.setStatus("FAILED");
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLogRepository.save(auditLog);
    }

    // Compliance and reporting methods
    public List<AuditLog> getUserActivityHistory(Long userId, int days) {
        Timestamp startDate = new Timestamp(System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000));
        return auditLogRepository.findUserActivitySince(userId, startDate);
    }

    public List<AuditLog> getSecurityEvents(Timestamp start, Timestamp end) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
    }

    public Long getActionCountSince(Long userId, String action, Duration duration) {
        Timestamp startTime = new Timestamp(System.currentTimeMillis() - duration.toMillis());
        return auditLogRepository.countActionsSince(userId, action, startTime);
    }

    // Check for suspicious activity
    public boolean hasSuspiciousActivity(Long userId) {
        Timestamp oneHourAgo = new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000));

        // Check for multiple failed login attempts
        Long failedLogins = auditLogRepository.countActionsSince(userId, "LOGIN_FAILED", oneHourAgo);
        if (failedLogins >= 5) {
            return true;
        }

        // Check for multiple password change attempts
        Long passwordChanges = auditLogRepository.countActionsSince(userId, "PASSWORD_CHANGE_ATTEMPT", oneHourAgo);
        if (passwordChanges >= 3) {
            return true;
        }

        return false;
    }
}