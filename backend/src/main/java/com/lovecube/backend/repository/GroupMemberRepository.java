package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupIdOrderByJoinedAtAsc(String groupId);

    Optional<GroupMember> findByGroupIdAndUserId(String groupId, Long userId);

    boolean existsByGroupIdAndUserId(String groupId, Long userId);

    long countByGroupId(String groupId);

    void deleteByGroupIdAndUserId(String groupId, Long userId);
}
