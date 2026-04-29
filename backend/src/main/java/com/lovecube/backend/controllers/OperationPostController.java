package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.OperationPost;
import com.lovecube.backend.services.OperationPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operation")
public class OperationPostController {

    private final OperationPostService service;

    public OperationPostController(OperationPostService service) {
        this.service = service;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<OperationPost>> getPosts(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String scope,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.getPosts(type, scope, limit));
    }

    @GetMapping("/posts/latest")
    public ResponseEntity<?> getLatest() {
        OperationPost post = service.getLatest();
        if (post == null) {
            return ResponseEntity.ok(Map.of());
        }
        return ResponseEntity.ok(post);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        OperationPost post = service.getById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(post);
    }
}
