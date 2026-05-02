package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatCheckinComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlatCheckinCommentRepository extends JpaRepository<PlatCheckinComment, Long> {

    Page<PlatCheckinComment> findByCheckinIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long checkinId, Pageable pageable);

    long countByCheckinIdAndDeletedAtIsNull(Long checkinId);

    @Query("SELECT c.checkinId, COUNT(c) FROM PlatCheckinComment c WHERE c.checkinId IN :ids AND c.deletedAt IS NULL GROUP BY c.checkinId")
    List<Object[]> countByCheckinIdInGrouped(@Param("ids") List<Long> ids);
}
