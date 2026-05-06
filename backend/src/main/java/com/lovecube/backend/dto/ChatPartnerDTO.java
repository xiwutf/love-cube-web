package com.lovecube.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatPartnerDTO {
    private Long userId;
    private String username;
    /** 用户头像 URL（主字段名，与全项目 API 治理一致） */
    private String avatarUrl;
    private String lastMessage; // 最近一条消息
    private String lastMessageTime; // 消息时间
    private int unreadCount;

    // 这里添加一个匹配 Object[] 参数的构造函数
    public ChatPartnerDTO(Object[] obj) {
        this.userId = ((Number) obj[0]).longValue(); // 转换 ID
        this.username = (String) obj[1];
        this.avatarUrl = (String) obj[2];
        this.lastMessage = (String) obj[3];
        this.lastMessageTime = (String) obj[4];
        this.unreadCount = (int) obj[5];
    }
}


