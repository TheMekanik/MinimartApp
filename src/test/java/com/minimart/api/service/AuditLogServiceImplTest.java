package com.minimart.api.service;

import com.minimart.api.entity.AuditLog;
import com.minimart.api.repository.AuditLogRepository;
import com.minimart.api.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    void recordSetsCreatedAtAndPersistsAuditLog() {
        AuditLogService service = new AuditLogServiceImpl(auditLogRepository);
        AuditLog auditLog = new AuditLog();

        service.record(auditLog);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertNotNull(captor.getValue().getCreatedAt());
    }
}
