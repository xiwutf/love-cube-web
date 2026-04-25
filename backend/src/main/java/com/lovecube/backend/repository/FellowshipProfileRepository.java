package com.lovecube.backend.repository;

import com.lovecube.backend.entity.FellowshipProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FellowshipProfileRepository extends JpaRepository<FellowshipProfile, Long> {
    Optional<FellowshipProfile> findByUserId(Long userId);
}

