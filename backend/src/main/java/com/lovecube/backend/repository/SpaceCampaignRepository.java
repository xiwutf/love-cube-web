package com.lovecube.backend.repository;

import com.lovecube.backend.entity.SpaceCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpaceCampaignRepository extends JpaRepository<SpaceCampaign, Long> {
    List<SpaceCampaign> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    Optional<SpaceCampaign> findFirstByGroupIdAndStatusOrderByStartDateDescCreatedAtDesc(
            Long groupId, String status);

    List<SpaceCampaign> findByGroupIdAndStatusOrderByStartDateDesc(Long groupId, String status);

    List<SpaceCampaign> findByStatusIn(Collection<String> statuses);
}
