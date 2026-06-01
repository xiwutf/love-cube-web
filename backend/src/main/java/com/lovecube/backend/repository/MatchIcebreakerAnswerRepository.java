package com.lovecube.backend.repository;

import com.lovecube.backend.entity.MatchIcebreakerAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchIcebreakerAnswerRepository extends JpaRepository<MatchIcebreakerAnswer, Long> {
    List<MatchIcebreakerAnswer> findByMatchPairKeyAndUserId(String matchPairKey, Long userId);

    long countByMatchPairKeyAndUserId(String matchPairKey, Long userId);

    Optional<MatchIcebreakerAnswer> findByMatchPairKeyAndUserIdAndQuestionId(
            String matchPairKey, Long userId, Long questionId);
}
