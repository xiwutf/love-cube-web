package com.lovecube.backend.repository;

import com.lovecube.backend.entity.FellowshipProfileMain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FellowshipProfileMainRepository extends JpaRepository<FellowshipProfileMain, Long> {
    Optional<FellowshipProfileMain> findByUserId(Long userId);

    List<FellowshipProfileMain> findByUserIdIn(Collection<Long> userIds);
}
