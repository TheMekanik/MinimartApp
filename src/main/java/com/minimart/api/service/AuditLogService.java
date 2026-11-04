package com.minimart.api.service;

import com.minimart.api.entity.AuditLog;

public interface AuditLogService {

    void record(AuditLog auditLog);
}
