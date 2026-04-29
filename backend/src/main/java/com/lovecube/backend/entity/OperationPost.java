package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_post")
public class OperationPost {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    /** notice / changelog / news / featured / activity */
    @Column(nullable = false)
    private String type;

    /** all / platform / fellowship */
    @Column(nullable = false)
    private String scope;

    /** draft / published */
    @Column(nullable = false)
    private String status;

    @Column(name = "is_top")
    private Boolean isTop;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
