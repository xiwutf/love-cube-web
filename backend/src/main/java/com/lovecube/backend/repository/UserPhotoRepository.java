package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserPhotoRepository extends JpaRepository<UserPhoto, Long> {
    List<UserPhoto> findByUserIdOrderBySortOrderAscIdAsc(Long userId);

    long countByUserId(Long userId);

    @Query("select p.userId, count(p.id) from UserPhoto p where p.userId in :userIds group by p.userId")
    List<Object[]> countGroupedByUserIds(@Param("userIds") List<Long> userIds);

    void deleteByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);
}
