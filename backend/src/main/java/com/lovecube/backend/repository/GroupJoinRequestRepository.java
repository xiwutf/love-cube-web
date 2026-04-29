package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long> {

    Optional<GroupJoinRequest> findTopByGroupIdAndUserIdAndStatusOrderByRequestedAtDesc(
            String groupId, Long userId, String status);

    List<GroupJoinRequest> findByGroupIdAndStatusOrderByRequestedAtDesc(String groupId, String status);

    List<GroupJoinRequest> findByGroupIdOrderByRequestedAtDesc(String groupId);

    boolean existsByGroupIdAndUserIdAndStatus(String groupId, Long userId, String status);
}
