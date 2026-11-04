package com.minimart.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "resource", nullable = false, length = 100)
    private String resource;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "request_path", nullable = false, length = 255)
    private String requestPath;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "actor", nullable = false, length = 100)
    private String actor;

    @Column(name = "request_id", nullable = false, length = 100)
    private String requestId;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "outcome", nullable = false, length = 20)
    private String outcome;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "error_type", length = 255)
    private String errorType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
