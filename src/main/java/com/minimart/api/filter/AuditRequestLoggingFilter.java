package com.minimart.api.filter;

import com.minimart.api.entity.AuditLog;
import com.minimart.api.service.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditRequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final AuditLogService auditLogService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isAuditableRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestId = resolveRequestId(request);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        String errorType = null;
        long startedAt = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
            statusCode = response.getStatus();
        } catch (RuntimeException | ServletException | IOException exception) {
            errorType = exception.getClass().getSimpleName();
            throw exception;
        } finally {
            if (errorType == null || response.getStatus() >= 400) {
                statusCode = response.getStatus();
            }

            try {
                auditLogService.record(buildAuditLog(
                        request,
                        requestId,
                        statusCode,
                        errorType,
                        System.currentTimeMillis() - startedAt
                ));
            } catch (RuntimeException auditException) {
                log.error(
                        "Unable to persist audit event: requestId={}, method={}, path={}, durationMs={}",
                        requestId,
                        request.getMethod(),
                        request.getRequestURI(),
                        System.currentTimeMillis() - startedAt,
                        auditException
                );
            }
        }
    }

    private AuditLog buildAuditLog(
            HttpServletRequest request,
            String requestId,
            int statusCode,
            String errorType,
            long durationMs
    ) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(resolveAction(request.getMethod(), request.getRequestURI()));
        auditLog.setResource(resolveResource(request.getRequestURI()));
        auditLog.setHttpMethod(request.getMethod());
        auditLog.setRequestPath(request.getRequestURI());
        auditLog.setResourceId(resolveResourceId(request.getRequestURI()));
        auditLog.setActor(resolveActor());
        auditLog.setRequestId(requestId);
        auditLog.setStatusCode(statusCode);
        auditLog.setDurationMs(durationMs);
        auditLog.setOutcome(statusCode >= 400 ? "FAILURE" : "SUCCESS");
        auditLog.setClientIp(resolveClientIp(request));
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setErrorType(errorType);
        return auditLog;
    }

    private String resolveAction(String method, String path) {
        if ("DELETE".equalsIgnoreCase(method) || path.contains("/delete-")) {
            return "DELETE";
        }
        if ("POST".equalsIgnoreCase(method)) {
            return "CREATE";
        }
        return "UPDATE";
    }

    private String resolveResource(String path) {
        String[] segments = path.split("/");
        return segments.length > 3 ? segments[3] : "unknown";
    }

    private String resolveResourceId(String path) {
        String[] segments = path.split("/");
        String lastSegment = segments.length == 0 ? "" : segments[segments.length - 1];
        return lastSegment.matches("\\d+") ? lastSegment : null;
    }

    private String resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "anonymous";
        }
        return authentication.getName();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        return forwardedFor == null || forwardedFor.isBlank()
                ? request.getRemoteAddr()
                : forwardedFor.split(",")[0].trim();
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        return requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId;
    }

    private boolean isAuditableRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return path.startsWith("/api/v1/")
                && !path.startsWith("/api/v1/auth")
                && ("POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method));
    }
}
