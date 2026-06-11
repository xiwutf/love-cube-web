package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GrowthCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrowthCampaignRepository extends JpaRepository<GrowthCampaign, Long> {
    List<GrowthCampaign> findAllByOrderByCreatedAtDesc();
}
