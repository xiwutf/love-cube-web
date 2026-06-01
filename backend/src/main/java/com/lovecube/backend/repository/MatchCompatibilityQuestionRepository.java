package com.lovecube.backend.repository;

import com.lovecube.backend.entity.MatchCompatibilityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchCompatibilityQuestionRepository extends JpaRepository<MatchCompatibilityQuestion, Long> {
    List<MatchCompatibilityQuestion> findByEnabledOrderBySortNoAsc(Integer enabled);
}
