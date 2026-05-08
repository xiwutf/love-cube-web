package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByUserId(Long userId);

    List<GroupMember> findByGroupIdOrderByJoinedAtAsc(String groupId);

    List<GroupMember> findByGroupIdAndUserIdIn(String groupId, Collection<Long> userIds);

    Optional<GroupMember> findByGroupIdAndUserId(String groupId, Long userId);

    boolean existsByGroupIdAndUserId(String groupId, Long userId);

    long countByGroupId(String groupId);

    long countByGroupIdAndRole(String groupId, String role);

    void deleteByGroupIdAndUserId(String groupId, Long userId);
}
