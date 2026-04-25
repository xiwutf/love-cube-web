package com.lovecube.backend.controllers;

import com.lovecube.backend.services.WeChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wechat")
public class LoginController {

    @Autowired
    private WeChatService weChatService;

    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam String code)
    {
        if (!weChatService.isWeChatConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "WECHAT_CONFIG_MISSING",
                    "message", "微信登录未启用，请联系管理员配置 wechat.appid / wechat.secret"
            ));
        }
        Map<String, Object> response = weChatService.login(code);
        return ResponseEntity.ok(response);
    }

}
