package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.OperationPost;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.OperationPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/operation")
public class AdminOperationPostController {

    private final OperationPostService service;
    private final AdminAuthService adminAuthService;

    public AdminOperationPostController(OperationPostService service, AdminAuthService adminAuthService) {
        this.service = service;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<OperationPost>> listAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requireAdmin(authHeader);
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping("/posts")
    public ResponseEntity<OperationPost> create(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody OperationPost post) {
        adminAuthService.requireAdmin(authHeader);
        return ResponseEntity.ok(service.create(post));
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<OperationPost> update(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody OperationPost patch) {
        adminAuthService.requireAdmin(authHeader);
        return ResponseEntity.ok(service.update(id, patch));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {
        adminAuthService.requireAdmin(authHeader);
        service.delete(id);
        return ResponseEntity.ok(Map.of("success", true, "id", id));
    }
}
