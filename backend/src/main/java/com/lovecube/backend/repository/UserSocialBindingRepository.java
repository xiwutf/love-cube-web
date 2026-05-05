package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserSocialBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSocialBindingRepository extends JpaRepository<UserSocialBinding, Long> {

    List<UserSocialBinding> findByUserId(Long userId);

    Optional<UserSocialBinding> findByUserIdAndProvider(Long userId, String provider);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserSocialBinding b WHERE b.id = :id AND b.userId = :userId")
    int deleteForUser(@Param("id") Long id, @Param("userId") Long userId);
}
