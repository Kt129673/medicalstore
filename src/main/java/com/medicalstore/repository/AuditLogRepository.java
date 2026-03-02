package com.medicalstore.repository;

import com.medicalstore.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy, Pageable pageable);

    Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    @Query("""
           SELECT a FROM AuditLog a
           WHERE (:username IS NULL OR a.performedBy LIKE %:username%)
             AND (:action   IS NULL OR a.action = :action)
             AND (:from     IS NULL OR a.timestamp >= :from)
             AND (:to       IS NULL OR a.timestamp <= :to)
           ORDER BY a.timestamp DESC
           """)
    Page<AuditLog> search(
            @Param("username") String username,
            @Param("action")   String action,
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            Pageable pageable);

    List<AuditLog> findTop50ByOrderByTimestampDesc();
}
