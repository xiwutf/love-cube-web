package com.lovecube.backend.services;

import com.lovecube.backend.entity.PlatGroup;
import com.lovecube.backend.entity.PlatformGroupSeason;
import com.lovecube.backend.entity.PlatformGroupSeasonScore;
import com.lovecube.backend.repository.PlatGroupRepository;
import com.lovecube.backend.repository.PlatformGroupSeasonRepository;
import com.lovecube.backend.repository.PlatformGroupSeasonScoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class GroupSeasonService {

    private static final int CHECKIN_POINTS = 2;
    private static final int TASK_POINTS = 5;
    private static final int ACTIVITY_POINTS = 10;

    private final PlatformGroupSeasonRepository seasonRepository;
    private final PlatformGroupSeasonScoreRepository scoreRepository;
    private final PlatGroupRepository groupRepository;

    public GroupSeasonService(
            PlatformGroupSeasonRepository seasonRepository,
            PlatformGroupSeasonScoreRepository scoreRepository,
            PlatGroupRepository groupRepository
    ) {
        this.seasonRepository = seasonRepository;
        this.scoreRepository = scoreRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public PlatformGroupSeason ensureActiveSeason() {
        LocalDate today = LocalDate.now();
        Optional<PlatformGroupSeason> active = seasonRepository.findFirstByStatusOrderByStartDateDesc("ACTIVE");
        if (active.isPresent()) {
            PlatformGroupSeason s = active.get();
            if (!today.isAfter(s.getEndDate())) {
                return s;
            }
            s.setStatus("ENDED");
            seasonRepository.save(s);
        }
        int quarter = (today.getMonthValue() - 1) / 3 + 1;
        String code = today.getYear() + "Q" + quarter;
        PlatformGroupSeason season = new PlatformGroupSeason();
        season.setSeasonCode(code);
        season.setTitle(today.getYear() + " 年第 " + quarter + " 赛季");
        season.setStartDate(LocalDate.of(today.getYear(), (quarter - 1) * 3 + 1, 1));
        season.setEndDate(season.getStartDate().plusMonths(3).minusDays(1));
        season.setStatus("ACTIVE");
        return seasonRepository.save(season);
    }

    @Transactional
    public void recordCheckin(Long groupId) {
        addScore(groupId, CHECKIN_POINTS, "checkin");
    }

    @Transactional
    public void recordTaskClaim(Long groupId) {
        addScore(groupId, TASK_POINTS, "task");
    }

    @Transactional
    public void recordActivitySignup(Long groupId) {
        addScore(groupId, ACTIVITY_POINTS, "activity");
    }

    public Map<String, Object> getSeasonRankings(int page, int size) {
        PlatformGroupSeason season = ensureActiveSeason();
        Page<PlatformGroupSeasonScore> scores = scoreRepository.findBySeasonIdOrderByScoreDesc(
                season.getId(), PageRequest.of(Math.max(0, page - 1), Math.max(1, size)));

        Set<Long> groupIds = new HashSet<>();
        scores.getContent().forEach(s -> groupIds.add(s.getGroupId()));
        Map<Long, PlatGroup> groupMap = new HashMap<>();
        groupRepository.findAllById(groupIds).forEach(g -> groupMap.put(g.getId(), g));

        List<Map<String, Object>> items = new ArrayList<>();
        int rank = (page - 1) * size + 1;
        for (PlatformGroupSeasonScore s : scores.getContent()) {
            PlatGroup g = groupMap.get(s.getGroupId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", rank++);
            row.put("groupId", s.getGroupId());
            row.put("groupName", g != null ? g.getName() : "团体");
            row.put("score", s.getScore());
            row.put("checkinCount", s.getCheckinCount());
            row.put("taskCount", s.getTaskCount());
            row.put("activityCount", s.getActivityCount());
            items.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("seasonCode", season.getSeasonCode());
        result.put("seasonTitle", season.getTitle());
        result.put("startDate", season.getStartDate().toString());
        result.put("endDate", season.getEndDate().toString());
        result.put("items", items);
        result.put("page", page);
        result.put("size", size);
        result.put("total", scores.getTotalElements());
        return result;
    }

    public Map<String, Object> getGroupSeasonRank(Long groupId) {
        PlatformGroupSeason season = ensureActiveSeason();
        Optional<PlatformGroupSeasonScore> scoreOpt = scoreRepository.findBySeasonIdAndGroupId(season.getId(), groupId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("seasonCode", season.getSeasonCode());
        result.put("seasonTitle", season.getTitle());
        if (scoreOpt.isEmpty()) {
            result.put("score", 0);
            result.put("rank", null);
            return result;
        }
        PlatformGroupSeasonScore score = scoreOpt.get();
        long higher = scoreRepository.findBySeasonIdOrderByScoreDesc(season.getId(), PageRequest.of(0, 1000))
                .getContent().stream().filter(s -> s.getScore() > score.getScore()).count();
        result.put("score", score.getScore());
        result.put("rank", higher + 1);
        result.put("checkinCount", score.getCheckinCount());
        result.put("taskCount", score.getTaskCount());
        result.put("activityCount", score.getActivityCount());
        return result;
    }

    private void addScore(Long groupId, int points, String type) {
        if (groupId == null) return;
        PlatformGroupSeason season = ensureActiveSeason();
        PlatformGroupSeasonScore score = scoreRepository.findBySeasonIdAndGroupId(season.getId(), groupId)
                .orElseGet(() -> {
                    PlatformGroupSeasonScore s = new PlatformGroupSeasonScore();
                    s.setSeasonId(season.getId());
                    s.setGroupId(groupId);
                    s.setScore(0);
                    s.setCheckinCount(0);
                    s.setTaskCount(0);
                    s.setActivityCount(0);
                    return s;
                });
        score.setScore(score.getScore() + points);
        switch (type) {
            case "checkin" -> score.setCheckinCount(score.getCheckinCount() + 1);
            case "task" -> score.setTaskCount(score.getTaskCount() + 1);
            case "activity" -> score.setActivityCount(score.getActivityCount() + 1);
            default -> { }
        }
        scoreRepository.save(score);
    }
}
