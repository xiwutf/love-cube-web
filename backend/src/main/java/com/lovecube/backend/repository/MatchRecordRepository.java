package com.lovecube.backend.repository;

import com.lovecube.backend.models.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 匹配记录 DAO
 */
@Repository
public interface MatchRecordRepository extends JpaRepository<MatchRecord, Long>
{
    List<MatchRecord> findByUserId(Long userId);

    List<MatchRecord> findByUserIdAndMatchedUserId(Long userId, Long matchedUserId);

    @Query("SELECT m.matchedUserId FROM MatchRecord m WHERE m.userId = :userId AND m.matchedUserId IN :matchedUserIds")
    List<Long> findExistingMatchedUserIds(@Param("userId") Long userId,
                                          @Param("matchedUserIds") Collection<Long> matchedUserIds);
}
