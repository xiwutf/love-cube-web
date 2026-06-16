package com.lovecube.backend.repository;

import com.lovecube.backend.entity.InterestTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterestTopicRepository extends JpaRepository<InterestTopic, Long> {
    List<InterestTopic> findByEnabledOrderBySortNoAscHeatDesc(Integer enabled);

    Optional<InterestTopic> findFirstByTitle(String title);
}
