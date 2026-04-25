package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.FellowshipInviteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/fellowship/invite")
public class FellowshipInviteController {
    private final AdminAuthService adminAuthService;
    private final FellowshipInviteService fellowshipInviteService;

    public FellowshipInviteController(AdminAuthService adminAuthService, FellowshipInviteService fellowshipInviteService) {
        this.adminAuthService = adminAuthService;
        this.fellowshipInviteService = fellowshipInviteService;
    }

    @GetMapping("/my-code")
    public ResponseEntity<?> getMyCode(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = adminAuthService.requireUser(authHeader);
            return ResponseEntity.ok(fellowshipInviteService.getMyCodeSummary(currentUser));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("message", ex.getReason() == null ? "请求失败" : ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "请求失败"));
        }
    }

    @GetMapping("/my-invitees")
    public ResponseEntity<?> getMyInvitees(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = adminAuthService.requireUser(authHeader);
            return ResponseEntity.ok(fellowshipInviteService.getMyInvitees(currentUser));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("message", ex.getReason() == null ? "请求失败" : ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "请求失败"));
        }
    }
}

