package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "positive_share_daily_topic")
public class PositiveShareDailyTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_date", nullable = false, unique = true)
    private LocalDate topicDate;

    @Column(name = "topic_text", nullable = false, length = 300)
    private String topicText;

    @Column(name = "hint_text", length = 500)
    private String hintText;

    @Column(nullable = false)
    private Integer enabled;
}
