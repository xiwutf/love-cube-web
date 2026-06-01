package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "match_icebreaker_question")
public class MatchIcebreakerQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "sort_no", nullable = false)
    private Integer sortNo;

    @Column(nullable = false)
    private Integer enabled;
}
