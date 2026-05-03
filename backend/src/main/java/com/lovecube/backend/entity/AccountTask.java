package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "account_tasks")
public class AccountTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "check_type", nullable = false, length = 40)
    private String checkType;

    @Column(name = "reward_exp", nullable = false)
    private Integer rewardExp;

    @Column(name = "sort_no")
    private Integer sortNo;

    @Column(nullable = false)
    private Integer enabled;
}
