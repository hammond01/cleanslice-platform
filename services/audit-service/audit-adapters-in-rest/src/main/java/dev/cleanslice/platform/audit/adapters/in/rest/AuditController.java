package dev.cleanslice.platform.audit.adapters.in.rest;

import dev.cleanslice.platform.audit.domain.AuditLog;
import dev.cleanslice.platform.audit.adapters.out.jpa.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "Audit logs API")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/logs")
    @Operation(summary = "Get all audit logs")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        var logs = auditLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }
}
