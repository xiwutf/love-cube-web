package com.lovecube.backend.repository;

import com.lovecube.backend.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, String> {
    List<Announcement> findByStatusOrderByPublishDateDesc(String status);

    long countByPinnedTrue();

    long countByRecommendedTrue();

    @Query("SELECT a FROM Announcement a WHERE a.status = :status ORDER BY CASE WHEN a.pinned = true THEN 0 ELSE 1 END, a.publishDate DESC")
    List<Announcement> findByStatusPinnedFirst(@Param("status") String status);
}
