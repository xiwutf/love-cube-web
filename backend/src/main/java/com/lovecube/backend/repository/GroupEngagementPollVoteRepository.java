package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupEngagementPollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface GroupEngagementPollVoteRepository extends JpaRepository<GroupEngagementPollVote, Long> {

    List<GroupEngagementPollVote> findByPollIdAndUserId(Long pollId, Long userId);

    @Modifying
    @Transactional
    void deleteByPollIdAndUserId(Long pollId, Long userId);

    boolean existsByPollIdAndUserId(Long pollId, Long userId);

    @Query("SELECT v.optionId, COUNT(v) FROM GroupEngagementPollVote v WHERE v.pollId = :pollId GROUP BY v.optionId")
    List<Object[]> countVotesByPollGrouped(@Param("pollId") Long pollId);

    @Query("SELECT COUNT(DISTINCT v.userId) FROM GroupEngagementPollVote v WHERE v.pollId = :pollId")
    long countDistinctVotersByPollId(@Param("pollId") Long pollId);

    @Query("SELECT DISTINCT v.pollId FROM GroupEngagementPollVote v WHERE v.userId = :userId AND v.pollId IN :pollIds")
    List<Long> findPollIdsVotedByUserIn(@Param("userId") Long userId, @Param("pollIds") Collection<Long> pollIds);
}
