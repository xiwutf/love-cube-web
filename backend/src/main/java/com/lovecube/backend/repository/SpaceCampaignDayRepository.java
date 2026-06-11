package com.lovecube.backend.repository;

import com.lovecube.backend.entity.SpaceCampaignDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpaceCampaignDayRepository extends JpaRepository<SpaceCampaignDay, Long> {
    List<SpaceCampaignDay> findByCampaignIdOrderByDayNumberAsc(Long campaignId);

    Optional<SpaceCampaignDay> findByCampaignIdAndDayNumber(Long campaignId, Integer dayNumber);
}
