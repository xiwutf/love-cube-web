package com.lovecube.backend.repository;

import com.lovecube.backend.entity.MatchIcebreakerQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchIcebreakerQuestionRepository extends JpaRepository<MatchIcebreakerQuestion, Long> {
    List<MatchIcebreakerQuestion> findByEnabledOrderBySortNoAsc(Integer enabled);
}
