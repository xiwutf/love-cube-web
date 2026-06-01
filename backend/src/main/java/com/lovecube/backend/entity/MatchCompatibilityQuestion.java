package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "match_compatibility_question")
public class MatchCompatibilityQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "option_a", nullable = false, length = 200)
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 200)
    private String optionB;

    @Column(name = "option_c", nullable = false, length = 200)
    private String optionC;

    @Column(name = "option_d", nullable = false, length = 200)
    private String optionD;

    @Column(name = "sort_no", nullable = false)
    private Integer sortNo;

    @Column(nullable = false)
    private Integer enabled;
}
