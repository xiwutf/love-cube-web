package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "daily_tasks")
public class DailyTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "target_count", nullable = false)
    private Integer targetCount;

    @Column(name = "reward_exp", nullable = false)
    private Integer rewardExp;

    @Column(name = "sort_no")
    private Integer sortNo;

    @Column(nullable = false)
    private Integer enabled;
}
