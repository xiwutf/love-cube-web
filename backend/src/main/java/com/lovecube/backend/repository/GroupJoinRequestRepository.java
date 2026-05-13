package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long> {

    long countByStatus(String status);

    long countByRequestedAtGreaterThanEqual(LocalDateTime since);

    List<GroupJoinRequest> findByUserIdAndGroupIdInAndStatus(Long userId, Collection<String> groupIds, String status);

    Optional<GroupJoinRequest> findTopByGroupIdAndUserIdAndStatusOrderByRequestedAtDesc(
            String groupId, Long userId, String status);

    List<GroupJoinRequest> findByGroupIdAndStatusOrderByRequestedAtDesc(String groupId, String status);

    List<GroupJoinRequest> findByGroupIdOrderByRequestedAtDesc(String groupId);

    boolean existsByGroupIdAndUserIdAndStatus(String groupId, Long userId, String status);

    List<GroupJoinRequest> findByUserIdAndStatusOrderByRequestedAtDesc(Long userId, String status);
}
