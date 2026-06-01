package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.lovecube.backend.services.VipService vipService;

    private static final Map<String, Integer> PACKAGE_MONTHS = Map.of(
        "month",  1,
        "season", 3,
        "year",   12
    );

    private static final Map<String, Integer> PACKAGE_PRICES = Map.of(
        "month",  30,
        "season", 80,
        "year",   280
    );

    /**
     * POST /api/payment/vip
     * Body: { packageId: "month"|"season"|"year", packageName: "月卡"|..., price: 30|... }
     */
    @PostMapping("/vip")
    public ResponseEntity<?> buyVip(@RequestBody Map<String, Object> body,
                                    @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        String packageId = String.valueOf(body.getOrDefault("packageId", "month"));
        Integer months = PACKAGE_MONTHS.get(packageId);
        if (months == null) {
            return ResponseEntity.badRequest().body("无效的套餐类型");
        }

        // 若已有未过期 VIP，在原到期时间上续期；否则从现在开始计算
        LocalDateTime base = (user.getVipExpiresAt() != null && user.getVipExpiresAt().isAfter(LocalDateTime.now()))
            ? user.getVipExpiresAt()
            : LocalDateTime.now();
        user.setVipStatus(packageId);
        user.setVipExpiresAt(base.plusMonths(months));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "message",   "VIP 开通成功",
            "vipStatus", user.getVipStatus(),
            "expiresAt", user.getVipExpiresAt().toString(),
            "vipActive", true
        ));
    }

    @GetMapping("/vip/status")
    public ResponseEntity<?> getVipStatus(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }
        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }
        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }
        return ResponseEntity.ok(vipService.buildVipStatus(user));
    }
}
