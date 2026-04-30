package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserGrowth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGrowthRepository extends JpaRepository<UserGrowth, Long> {
    Optional<UserGrowth> findByUserId(Long userId);
}
