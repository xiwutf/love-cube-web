package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserInviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInviteCodeRepository extends JpaRepository<UserInviteCode, Long> {
    Optional<UserInviteCode> findByUserId(Long userId);

    Optional<UserInviteCode> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);
}
