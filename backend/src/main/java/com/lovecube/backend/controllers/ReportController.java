package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.ReportRecord;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.ReportRecordRepository;
import com.lovecube.backend.services.BlacklistService;
import com.lovecube.backend.services.UnifiedProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportRecordRepository reportRecordRepository;
    private final BlacklistService blacklistService;
    private final UnifiedProfileService unifiedProfileService;

    public ReportController(ReportRecordRepository reportRecordRepository,
                            BlacklistService blacklistService,
                            UnifiedProfileService unifiedProfileService) {
        this.reportRecordRepository = reportRecordRepository;
        this.blacklistService = blacklistService;
        this.unifiedProfileService = unifiedProfileService;
    }

    @PostMapping
    public ResponseEntity<?> submitReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            Long myId = currentUser.getUserid();

            Long targetUserId = body.get("targetUserId") != null
                ? Long.parseLong(body.get("targetUserId").toString()) : null;

            if (myId.equals(targetUserId)) {
                return ResponseEntity.badRequest().body(Map.of("message", "不能举报自己"));
            }

            ReportRecord record = new ReportRecord();
            record.setId("report-" + UUID.randomUUID());
            record.setReporterId(myId);
            record.setTargetUserId(targetUserId);
            record.setTargetType(str(body, "targetType", "USER"));
            record.setTargetId(str(body, "targetId", null));
            record.setReasonType(str(body, "reasonType", "其他"));
            record.setReportType(record.getReasonType());
            record.setContent(str(body, "content", null));
            record.setStatus("PENDING");
            record.setCreatedAt(LocalDateTime.now());
            reportRecordRepository.save(record);

            boolean alsoBlock = Boolean.TRUE.equals(body.get("alsoBlock"));
            if (alsoBlock && targetUserId != null) {
                try {
                    blacklistService.blockUser(myId, targetUserId, "举报时拉黑");
                } catch (Exception ignored) {
                }
            }

            return ResponseEntity.ok(Map.of("message", "举报已提交", "id", record.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "提交失败: " + e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyReports(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            List<ReportRecord> records = reportRecordRepository
                .findByReporterIdOrderByCreatedAtDesc(currentUser.getUserid());
            return ResponseEntity.ok(records);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    private String str(Map<String, Object> body, String key, String defaultVal) {
        Object v = body.get(key);
        if (v == null || v.toString().isBlank()) return defaultVal;
        return v.toString();
    }
}
