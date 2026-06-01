package com.lovecube.backend.repository;

import com.lovecube.backend.entity.InterestTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestTopicRepository extends JpaRepository<InterestTopic, Long> {
    List<InterestTopic> findByEnabledOrderBySortNoAscHeatDesc(Integer enabled);
}
