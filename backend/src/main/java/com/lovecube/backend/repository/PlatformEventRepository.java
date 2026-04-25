package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatformEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformEventRepository extends JpaRepository<PlatformEvent, String> {
    List<PlatformEvent> findByStatusOrderByEventTimeDesc(String status);
}
