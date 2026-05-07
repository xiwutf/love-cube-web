package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserFeedback;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFeedbackRepository extends JpaRepository<UserFeedback, String> {
    List<UserFeedback> findAllByOrderByCreatedAtDesc();

    List<UserFeedback> findByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatusNot(String status);
}
