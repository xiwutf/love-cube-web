package com.lovecube.backend.controllers;

import com.lovecube.backend.services.PortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/home")
public class PortalController {

    private final PortalService portalService;

    public PortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/portal")
    public ResponseEntity<Map<String, Object>> portal() {
        return ResponseEntity.ok(portalService.buildPortal());
    }
}
