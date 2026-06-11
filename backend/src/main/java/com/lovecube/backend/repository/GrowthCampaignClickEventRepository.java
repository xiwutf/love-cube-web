package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GrowthCampaignClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrowthCampaignClickEventRepository extends JpaRepository<GrowthCampaignClickEvent, Long> {
}
