package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.PlatformMemberService;
import com.lovecube.backend.services.PlatformMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/platform/member")
public class PlatformMemberController {

    private final PlatformMemberService memberService;
    private final AdminAuthService adminAuthService;
    private final UserRepository userRepository;

    public PlatformMemberController(
            PlatformMemberService memberService,
            AdminAuthService adminAuthService,
            UserRepository userRepository
    ) {
        this.memberService = memberService;
        this.adminAuthService = adminAuthService;
        this.userRepository = userRepository;
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestHeader("Authorization") String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        return ResponseEntity.ok(memberService.buildStatus(user));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "请先登录"));
        }
        memberService.activateMonthly(user);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "message", "平台会员开通成功（演示）",
                "memberActive", true,
                "expiresAt", user.getPlatformMemberExpiresAt().toString()
        ));
    }

    private User resolveUser(String authHeader) {
        try {
            return adminAuthService.requireUser(authHeader);
        } catch (Exception e) {
            return null;
        }
    }
}
