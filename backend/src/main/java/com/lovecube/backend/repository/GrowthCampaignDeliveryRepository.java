package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GrowthCampaignDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrowthCampaignDeliveryRepository extends JpaRepository<GrowthCampaignDelivery, Long> {
    Page<GrowthCampaignDelivery> findByCampaignIdOrderByIdAsc(Long campaignId, Pageable pageable);

    List<GrowthCampaignDelivery> findByCampaignId(Long campaignId);

    long countByCampaignIdAndStatus(Long campaignId, String status);

    java.util.Optional<GrowthCampaignDelivery> findByNotificationId(Long notificationId);
}
