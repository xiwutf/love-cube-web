package com.lovecube.backend.repository;

import com.lovecube.backend.models.UserBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlacklistRepository extends JpaRepository<UserBlacklist, Long> {

    List<UserBlacklist> findByUserId(Long userId);

    Optional<UserBlacklist> findByUserIdAndBlockedUserId(Long userId, Long blockedUserId);

    boolean existsByUserIdAndBlockedUserId(Long userId, Long blockedUserId);

    @Query("SELECT b.blockedUserId FROM UserBlacklist b WHERE b.userId = :userId")
    List<Long> findBlockedUserIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT b.userId FROM UserBlacklist b WHERE b.blockedUserId = :userId")
    List<Long> findBlockerUserIdsByUserId(@Param("userId") Long userId);
}
