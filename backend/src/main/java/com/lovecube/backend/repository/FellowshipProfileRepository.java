package com.lovecube.backend.repository;

import com.lovecube.backend.entity.FellowshipProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FellowshipProfileRepository extends JpaRepository<FellowshipProfile, Long> {
    Optional<FellowshipProfile> findByUserId(Long userId);

    List<FellowshipProfile> findByUserIdIn(Collection<Long> userIds);

    List<FellowshipProfile> findByIdentityRoleIn(List<String> roles);

    @Query("SELECT fp.userId FROM FellowshipProfile fp WHERE fp.identityRole IN :roles")
    List<Long> findUserIdsByIdentityRoleIn(@Param("roles") List<String> roles);

    @Query("""
            SELECT COUNT(f) FROM FellowshipProfile f
            WHERE f.nickname IS NOT NULL AND TRIM(f.nickname) <> ''
            AND f.avatarUrl IS NOT NULL AND TRIM(f.avatarUrl) <> ''""")
    long countWithNicknameAndAvatar();
}
