package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_social_bindings")
public class UserSocialBinding {

    public static final String PROVIDER_WECHAT_OFFICIAL = "WECHAT_OFFICIAL";
    public static final String PROVIDER_WECHAT_MINI_PROGRAM = "WECHAT_MINI_PROGRAM";
    public static final String PROVIDER_QQ = "QQ";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "provider", length = 64, nullable = false)
    private String provider;

    @Column(name = "openid", length = 128, nullable = false)
    private String openid;

    @Column(name = "unionid", length = 128)
    private String unionid;

    @Column(name = "nickname", length = 128)
    private String nickname;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "bind_status", length = 32, nullable = false)
    private String bindStatus = "BOUND";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (bindStatus == null) bindStatus = "BOUND";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
