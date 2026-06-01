package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PositiveShareDailyTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PositiveShareDailyTopicRepository extends JpaRepository<PositiveShareDailyTopic, Long> {
    Optional<PositiveShareDailyTopic> findFirstByTopicDateAndEnabled(LocalDate topicDate, Integer enabled);

    Optional<PositiveShareDailyTopic> findFirstByEnabledOrderByTopicDateDesc(Integer enabled);
}
