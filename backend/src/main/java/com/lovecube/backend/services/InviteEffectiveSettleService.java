package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserInviteRelation;
import com.lovecube.backend.growth.entity.GrowthEvent;
import com.lovecube.backend.growth.enums.SettleStatus;
import com.lovecube.backend.growth.repository.GrowthEventRepository;
import com.lovecube.backend.growth.service.GrowthEngine;
import com.lovecube.backend.growth.service.GrowthRewardService;
import com.lovecube.backend.repository.UserGrowthLogRepository;
import com.lovecube.backend.repository.UserInviteRelationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InviteEffectiveSettleService {

    private static final int REQUIRED_LOGIN_DAYS = 3;

    private final UserInviteRelationRepository inviteRelationRepository;
    private final UserGrowthLogRepository growthLogRepository;
    private final GrowthEventRepository growthEventRepository;
    private final GrowthEngine growthEngine;
    private final GrowthRewardService growthRewardService;

    public InviteEffectiveSettleService(
            UserInviteRelationRepository inviteRelationRepository,
            UserGrowthLogRepository growthLogRepository,
            GrowthEventRepository growthEventRepository,
            GrowthEngine growthEngine,
            GrowthRewardService growthRewardService
    ) {
        this.inviteRelationRepository = inviteRelationRepository;
        this.growthLogRepository = growthLogRepository;
        this.growthEventRepository = growthEventRepository;
        this.growthEngine = growthEngine;
        this.growthRewardService = growthRewardService;
    }

    /**
     * 尝试将被邀请人标记为有效邀请并结算 PENDING 成长事件。
     * 条件：累计登录 ≥3 天 且 已完善资料。
     */
    @Transactional
    public boolean trySettleForInvitee(Long inviteeUserId) {
        Optional<UserInviteRelation> relationOpt = inviteRelationRepository
                .findByInvitedUserIdAndStatus(inviteeUserId, "SUCCESS");
        if (relationOpt.isEmpty()) {
            return false;
        }
        if (!meetsEffectiveCriteria(inviteeUserId)) {
            return false;
        }

        UserInviteRelation relation = relationOpt.get();
        relation.setStatus("EFFECTIVE");
        relation.setEffectiveAt(LocalDateTime.now());
        inviteRelationRepository.save(relation);

        settleGrowthEvent(relation.getInviterUserId(), inviteeUserId);

        try {
            long registeredCount = inviteRelationRepository
                    .countByInviterUserIdAndStatus(relation.getInviterUserId(), "EFFECTIVE")
                    + inviteRelationRepository.countByInviterUserIdAndStatus(relation.getInviterUserId(), "SUCCESS");
            growthRewardService.checkAndGrantInviteMilestoneRewards(
                    relation.getInviterUserId(), (int) registeredCount);
        } catch (Exception ignored) {
            // milestone must not break settle
        }
        return true;
    }

    private boolean meetsEffectiveCriteria(Long inviteeUserId) {
        long loginDays = growthLogRepository.countByUserIdAndActionType(inviteeUserId, "LOGIN");
        return loginDays >= REQUIRED_LOGIN_DAYS;
    }

    private void settleGrowthEvent(Long inviterUserId, Long inviteeUserId) {
        String dedupeKey = "growth:user_invited_effective:inviter:" + inviterUserId + ":invitee:" + inviteeUserId;
        GrowthEvent event = growthEventRepository.findByDedupeKey(dedupeKey).orElse(null);
        if (event == null) return;
        if (SettleStatus.SETTLED.name().equals(event.getSettleStatus())) return;

        event.setSettleStatus(SettleStatus.SETTLED.name());
        growthEventRepository.save(event);
        growthEngine.consume(event);
    }
}
