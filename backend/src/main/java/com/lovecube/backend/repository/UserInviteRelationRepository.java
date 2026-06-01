package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserInviteRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInviteRelationRepository extends JpaRepository<UserInviteRelation, Long> {
    boolean existsByInvitedUserId(Long invitedUserId);

    long countByInviterUserIdAndStatus(Long inviterUserId, String status);

    Optional<UserInviteRelation> findByInvitedUserIdAndStatus(Long invitedUserId, String status);

    List<UserInviteRelation> findByInviterUserIdAndStatusOrderByCreatedAtDesc(Long inviterUserId, String status);
}
