package com.lovecube.backend.repository;

import com.lovecube.backend.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, String> {
    List<Announcement> findByStatusOrderByPublishDateDesc(String status);
}
