package com.minimart.api.service.impl;

import com.minimart.api.entity.AuditLog;
import com.minimart.api.repository.AuditLogRepository;
import com.minimart.api.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void record(AuditLog auditLog) {
        auditLog.setCreatedAt(Instant.now());
        auditLogRepository.save(auditLog);
        log.info(
                "Audit event recorded: action={}, resource={}, resourceId={}, actor={}, status={}",
                auditLog.getAction(),
                auditLog.getResource(),
                auditLog.getResourceId(),
                auditLog.getActor(),
                auditLog.getStatusCode()
        );
    }
}
