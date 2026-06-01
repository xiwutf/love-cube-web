package com.lovecube.backend.repository;

import com.lovecube.backend.entity.MatchCompatibilityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchCompatibilityAnswerRepository extends JpaRepository<MatchCompatibilityAnswer, Long> {
    List<MatchCompatibilityAnswer> findByMatchPairKeyAndUserId(String matchPairKey, Long userId);

    Optional<MatchCompatibilityAnswer> findByMatchPairKeyAndUserIdAndQuestionId(
            String matchPairKey, Long userId, Long questionId);
}
