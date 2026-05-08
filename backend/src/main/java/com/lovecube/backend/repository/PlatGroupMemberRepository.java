package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlatGroupMemberRepository extends JpaRepository<PlatGroupMember, Long> {

    boolean existsByUserIdAndStatus(Long userId, String status);

    Optional<PlatGroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    List<PlatGroupMember> findByGroupIdAndUserIdIn(Long groupId, Collection<Long> userIds);

    List<PlatGroupMember> findByGroupIdAndStatusOrderByJoinedAtAsc(Long groupId, String status);

    List<PlatGroupMember> findByGroupIdOrderByJoinedAtAsc(Long groupId);

    List<PlatGroupMember> findByUserId(Long userId);

    long countByGroupIdAndStatusAndRole(Long groupId, String status, String role);
}
