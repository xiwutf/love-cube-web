package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPhotoRepository extends JpaRepository<UserPhoto, Long> {
    List<UserPhoto> findByUserIdOrderBySortOrderAscIdAsc(Long userId);

    void deleteByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);
}
