package com.mgaye.yonei.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mgaye.yonei.entity.AuditLog;
import com.mgaye.yonei.service.AuditService;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getUserAuditLogs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {

        List<AuditLog> logs = auditService.getUserActivityHistory(userId, days);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/security")
    public ResponseEntity<List<AuditLog>> getSecurityEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<AuditLog> logs = auditService.getSecurityEvents(
                Timestamp.valueOf(start),
                Timestamp.valueOf(end));
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/suspicious/{userId}")
    public ResponseEntity<Map<String, Object>> checkSuspiciousActivity(@PathVariable Long userId) {
        boolean isSuspicious = auditService.hasSuspiciousActivity(userId);
        List<AuditLog> recentActivity = auditService.getUserActivityHistory(userId, 1); // Last 24 hours

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("suspiciousActivity", isSuspicious);
        response.put("recentActivityCount", recentActivity.size());
        response.put("lastActivity", recentActivity.isEmpty() ? null : recentActivity.get(0));

        return ResponseEntity.ok(response);
    }
}