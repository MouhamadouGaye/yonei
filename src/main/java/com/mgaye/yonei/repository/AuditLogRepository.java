package com.mgaye.yonei.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mgaye.yonei.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    List<AuditLog> findByUserIdAndActionOrderByTimestampDesc(Long userId, String action);

    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(Timestamp start, Timestamp end);

    List<AuditLog> findByStatusOrderByTimestampDesc(String status);

    List<AuditLog> findByResourceTypeAndResourceIdOrderByTimestampDesc(String resourceType, Long resourceId);

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :startDate AND a.userId = :userId")
    List<AuditLog> findUserActivitySince(@Param("userId") Long userId, @Param("startDate") Timestamp startDate);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.action = :action AND a.timestamp >= :startTime")
    Long countActionsSince(@Param("userId") Long userId, @Param("action") String action,
            @Param("startTime") Timestamp startTime);
}