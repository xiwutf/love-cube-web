package com.lovecube.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.dating.DatingEventTemplate;
import com.lovecube.backend.dating.DatingParticipant;
import com.lovecube.backend.entity.DatingConnection;
import com.lovecube.backend.entity.DatingEventIdentity;
import com.lovecube.backend.entity.DatingEventProfile;
import com.lovecube.backend.entity.DatingLike;
import com.lovecube.backend.entity.DatingMutualMatch;
import com.lovecube.backend.entity.EventGuestParticipant;
import com.lovecube.backend.entity.EventSignup;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.DatingConnectionRepository;
import com.lovecube.backend.repository.DatingLikeRepository;
import com.lovecube.backend.repository.DatingMutualMatchRepository;
import com.lovecube.backend.repository.DatingEventIdentityRepository;
import com.lovecube.backend.repository.DatingEventProfileRepository;
import com.lovecube.backend.repository.EventGuestParticipantRepository;
import com.lovecube.backend.repository.EventSignupRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DatingEventService {

    private static final int BADGE_PAD = 2;

    private final PlatformEventRepository eventRepository;
    private final EventSignupRepository signupRepository;
    private final DatingEventIdentityRepository identityRepository;
    private final DatingEventProfileRepository profileRepository;
    private final DatingConnectionRepository connectionRepository;
    private final DatingLikeRepository likeRepository;
    private final DatingMutualMatchRepository mutualMatchRepository;
    private final EventGuestParticipantRepository guestRepository;
    private final UserRepository userRepository;
    private final UnifiedProfileService unifiedProfileService;
    private final DatingMutualMatchNotifyService mutualMatchNotifyService;
    private final ObjectMapper objectMapper;

    public DatingEventService(
            PlatformEventRepository eventRepository,
            EventSignupRepository signupRepository,
            DatingEventIdentityRepository identityRepository,
            DatingEventProfileRepository profileRepository,
            DatingConnectionRepository connectionRepository,
            DatingLikeRepository likeRepository,
            DatingMutualMatchRepository mutualMatchRepository,
            EventGuestParticipantRepository guestRepository,
            UserRepository userRepository,
            UnifiedProfileService unifiedProfileService,
            DatingMutualMatchNotifyService mutualMatchNotifyService,
            ObjectMapper objectMapper
    ) {
        this.eventRepository = eventRepository;
        this.signupRepository = signupRepository;
        this.identityRepository = identityRepository;
        this.profileRepository = profileRepository;
        this.connectionRepository = connectionRepository;
        this.likeRepository = likeRepository;
        this.mutualMatchRepository = mutualMatchRepository;
        this.guestRepository = guestRepository;
        this.userRepository = userRepository;
        this.unifiedProfileService = unifiedProfileService;
        this.mutualMatchNotifyService = mutualMatchNotifyService;
        this.objectMapper = objectMapper;
    }

    public boolean isDatingEvent(PlatformEvent event) {
        return event != null && DatingEventTemplate.isDating(event.getTemplateType());
    }

    public PlatformEvent requireDatingEvent(String eventId) {
        PlatformEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        if (!isDatingEvent(event)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该活动未启用联谊专场模板");
        }
        return event;
    }

    @Transactional
    public Map<String, Object> ensureIdentityAfterCheckin(User user, String eventId) {
        return ensureIdentityAfterCheckin(DatingParticipant.fromUser(user, eventId));
    }

    @Transactional
    public Map<String, Object> ensureIdentityAfterGuestCheckin(EventGuestParticipant guest, String eventId) {
        return ensureIdentityAfterCheckin(DatingParticipant.fromGuest(guest));
    }

    @Transactional
    public Map<String, Object> ensureIdentityAfterCheckin(DatingParticipant participant) {
        String eventId = participant.eventId();
        requireDatingEvent(eventId);
        requireCheckedInSignup(participant);
        Optional<DatingEventIdentity> existing = findIdentity(participant);
        if (existing.isPresent()) {
            return toIdentityMap(existing.get());
        }
        String genderSide = resolveGenderSide(participant);
        if (genderSide == null) {
            return Map.of(
                    "assigned", false,
                    "needGender", true,
                    "message", "请先在联谊专场中确认性别信息以分配活动编号"
            );
        }
        DatingEventIdentity identity = assignIdentity(participant, genderSide);
        return toIdentityMap(identity);
    }

    public String resolveTargetUrl(PlatformEvent event, boolean checkedIn) {
        if (!checkedIn) {
            return "/events/" + event.getId() + "/onsite";
        }
        if (DatingEventTemplate.isDating(event.getTemplateType())) {
            return "/fellowship/events/" + event.getId() + "/dating";
        }
        return "/events/" + event.getId();
    }

    public Optional<DatingEventIdentity> findIdentityForGuest(String eventId, Long guestParticipantId) {
        return identityRepository.findByEventIdAndGuestParticipantId(eventId, guestParticipantId);
    }

    public Optional<DatingEventProfile> findProfileForGuest(String eventId, Long guestParticipantId) {
        return profileRepository.findByEventIdAndGuestParticipantId(eventId, guestParticipantId);
    }

    @Transactional
    public DatingEventIdentity assignIdentityForGenderSide(User user, String eventId, String genderSide) {
        return assignIdentityForGenderSide(DatingParticipant.fromUser(user, eventId), genderSide);
    }

    @Transactional
    public DatingEventIdentity assignIdentityForGenderSide(DatingParticipant participant, String genderSide) {
        requireDatingEvent(participant.eventId());
        requireCheckedInSignup(participant);
        String normalized = normalizeGenderSide(genderSide);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择有效的性别");
        }
        return findIdentity(participant)
                .orElseGet(() -> assignIdentity(participant, normalized));
    }

    public Map<String, Object> getContext(User user, String eventId) {
        return getContext(DatingParticipant.fromUser(user, eventId));
    }

    @Transactional
    public Map<String, Object> getContext(DatingParticipant participant) {
        String eventId = participant.eventId();
        PlatformEvent event = requireDatingEvent(eventId);
        EventSignup signup = findSignup(participant).orElse(null);
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("eventId", event.getId());
        ctx.put("eventTitle", event.getTitle());
        ctx.put("templateType", event.getTemplateType());
        ctx.put("signedUp", signup != null);
        boolean checkedIn = signup != null && Boolean.TRUE.equals(signup.getCheckedIn());
        ctx.put("checkedIn", checkedIn);
        ctx.put("eventTime", event.getEventTime());
        ctx.put("endTime", event.getEndTime());
        ctx.put("effectiveEndTime", PlatformEventLifecycle.resolveEnd(event));
        ctx.put("guestMode", participant.isGuest());
        if (participant.isGuest()) {
            ctx.put("nickname", participant.nickname());
        }

        Optional<DatingEventIdentity> identity = findIdentity(participant);
        if (identity.isEmpty() && checkedIn) {
            ensureIdentityAfterCheckin(participant);
            identity = findIdentity(participant);
        }
        identity.ifPresent(i -> ctx.put("identity", toIdentityMap(i)));

        Optional<DatingEventProfile> profile = findProfile(participant);
        ctx.put("profileCompleted", profile.map(DatingEventProfile::getCompleted).orElse(false));
        ctx.put("needGender", identity.isEmpty() && resolveGenderSide(participant) == null);
        return ctx;
    }

    public Map<String, Object> getMyIdentity(User user, String eventId) {
        return getMyIdentity(DatingParticipant.fromUser(user, eventId));
    }

    public Map<String, Object> getMyIdentity(DatingParticipant participant) {
        requireCheckedInSignup(participant);
        DatingEventIdentity identity = findIdentity(participant)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "尚未分配活动编号，请完善资料或确认性别"));
        return toIdentityMap(identity);
    }

    public Map<String, Object> getProfilePrefill(User user) {
        Map<String, Object> fellowship = unifiedProfileService.buildFellowshipPayload(user);
        Map<String, Object> prefill = new LinkedHashMap<>();
        prefill.put("age", fellowship.get("age"));
        prefill.put("heightCm", fellowship.get("height"));
        prefill.put("city", fellowship.get("city"));
        prefill.put("occupation", fellowship.get("occupation"));
        prefill.put("education", fellowship.get("education"));
        prefill.put("interestTags", parseTagsString(fellowship.get("tags")));
        prefill.put("selfIntro", fellowship.get("bio"));
        prefill.put("partnerRequirements", fellowship.get("intention"));
        prefill.put("idealPartnerDesc", "");
        prefill.put("genderSide", mapGenderToSide((String) fellowship.get("gender")));
        return prefill;
    }

    public Map<String, Object> getMyProfile(User user, String eventId) {
        return getMyProfile(DatingParticipant.fromUser(user, eventId));
    }

    public Map<String, Object> getMyProfile(DatingParticipant participant) {
        requireCheckedInSignup(participant);
        DatingEventProfile profile = findProfile(participant).orElse(null);
        if (profile == null) {
            Map<String, Object> empty = participant.isGuest()
                    ? guestProfilePrefill(participant)
                    : getProfilePrefill(loadUser(participant.userId()));
            empty.put("completed", false);
            empty.put("saved", false);
            return empty;
        }
        return toProfileMap(profile);
    }

    @Transactional
    public Map<String, Object> saveMyProfile(User user, String eventId, Map<String, Object> payload) {
        return saveMyProfile(DatingParticipant.fromUser(user, eventId), payload);
    }

    @Transactional
    public Map<String, Object> saveMyProfile(DatingParticipant participant, Map<String, Object> payload) {
        String eventId = participant.eventId();
        requireCheckedInSignup(participant);
        String genderSideInput = payload != null ? stringVal(payload.get("genderSide")) : null;
        if (genderSideInput != null && !genderSideInput.isBlank()) {
            assignIdentityForGenderSide(participant, genderSideInput);
        } else if (findIdentity(participant).isEmpty()) {
            String fromParticipant = resolveGenderSide(participant);
            if (fromParticipant != null) {
                assignIdentity(participant, fromParticipant);
            }
        }

        DatingEventProfile profile = findProfile(participant)
                .orElseGet(() -> {
                    DatingEventProfile created = new DatingEventProfile();
                    created.setEventId(eventId);
                    if (participant.isGuest()) {
                        created.setGuestParticipantId(participant.guestParticipantId());
                    } else {
                        created.setUserId(participant.userId());
                    }
                    return created;
                });

        if (payload != null) {
            if (payload.containsKey("age")) profile.setAge(intVal(payload.get("age")));
            if (payload.containsKey("heightCm")) profile.setHeightCm(intVal(payload.get("heightCm")));
            if (payload.containsKey("city")) profile.setCity(trim(payload.get("city"), 64));
            if (payload.containsKey("occupation")) profile.setOccupation(trim(payload.get("occupation"), 128));
            if (payload.containsKey("education")) profile.setEducation(trim(payload.get("education"), 64));
            if (payload.containsKey("interestTags")) profile.setInterestTags(serializeTags(payload.get("interestTags")));
            if (payload.containsKey("selfIntro")) profile.setSelfIntro(trim(payload.get("selfIntro"), 2000));
            if (payload.containsKey("partnerRequirements")) {
                profile.setPartnerRequirements(trim(payload.get("partnerRequirements"), 1000));
            }
            if (payload.containsKey("idealPartnerDesc")) {
                profile.setIdealPartnerDesc(trim(payload.get("idealPartnerDesc"), 1000));
            }
        }
        profile.setCompleted(isProfileComplete(profile));
        profileRepository.save(profile);
        return toProfileMap(profile);
    }

    public Map<String, Object> getMyCard(User user, String eventId) {
        return getMyCard(DatingParticipant.fromUser(user, eventId));
    }

    public Map<String, Object> getMyCard(DatingParticipant participant) {
        return buildCard(participant, participant);
    }

    public Map<String, Object> getUserCard(User viewer, String eventId, Long targetUserId) {
        return getParticipantCard(DatingParticipant.fromUser(viewer, eventId), DatingParticipant.TYPE_REGISTERED + ":" + targetUserId);
    }

    public Map<String, Object> getParticipantCard(DatingParticipant viewer, String participantKey) {
        requireCheckedInSignup(viewer);
        DatingParticipant.ParsedParticipantKey parsed = DatingParticipant.parseKey(participantKey);
        DatingParticipant target = resolveParticipant(viewer.eventId(), parsed);
        requireRosterVisible(viewer.eventId(), target);
        return buildCard(target, viewer);
    }

    public Map<String, Object> getRoster(User user, String eventId) {
        return getRoster(DatingParticipant.fromUser(user, eventId));
    }

    @Transactional
    public Map<String, Object> createConnection(DatingParticipant viewer, String targetParticipantType, Long targetParticipantId) {
        requireCheckedInSignup(viewer);
        String eventId = viewer.eventId();
        requireDatingEvent(eventId);
        String normalizedType = normalizeParticipantType(targetParticipantType);
        if (targetParticipantId == null || targetParticipantId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "目标参与者无效");
        }
        DatingParticipant.ParsedParticipantKey parsed = new DatingParticipant.ParsedParticipantKey(normalizedType, targetParticipantId);
        DatingParticipant target = resolveParticipant(eventId, parsed);
        if (isSameParticipant(viewer, target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能记录认识自己");
        }
        requireRosterVisible(eventId, target);

        Optional<DatingConnection> existing = connectionRepository
                .findByEventIdAndParticipantTypeAndParticipantIdAndTargetParticipantTypeAndTargetParticipantId(
                        eventId,
                        viewer.participantType(),
                        participantIdValue(viewer),
                        target.participantType(),
                        participantIdValue(target)
                );
        if (existing.isPresent()) {
            return Map.of("success", true);
        }

        DatingConnection connection = new DatingConnection();
        connection.setEventId(eventId);
        connection.setParticipantType(viewer.participantType());
        connection.setParticipantId(participantIdValue(viewer));
        connection.setTargetParticipantType(target.participantType());
        connection.setTargetParticipantId(participantIdValue(target));
        connectionRepository.save(connection);
        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Object> deleteConnection(DatingParticipant viewer, Long connectionId) {
        requireCheckedInSignup(viewer);
        requireDatingEvent(viewer.eventId());
        DatingConnection connection = connectionRepository
                .findByIdAndEventIdAndParticipantTypeAndParticipantId(
                        connectionId,
                        viewer.eventId(),
                        viewer.participantType(),
                        participantIdValue(viewer)
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "认识记录不存在"));
        connectionRepository.delete(connection);
        return Map.of("success", true);
    }

    public List<Map<String, Object>> listMyConnections(DatingParticipant viewer) {
        requireCheckedInSignup(viewer);
        requireDatingEvent(viewer.eventId());
        return connectionRepository
                .findByEventIdAndParticipantTypeAndParticipantIdOrderByCreatedAtDesc(
                        viewer.eventId(),
                        viewer.participantType(),
                        participantIdValue(viewer)
                )
                .stream()
                .map(conn -> toConnectionListItem(viewer.eventId(), conn))
                .toList();
    }

    public Map<String, Object> getConnectionStats(DatingParticipant viewer) {
        requireCheckedInSignup(viewer);
        requireDatingEvent(viewer.eventId());
        long total = connectionRepository.countByEventIdAndParticipantTypeAndParticipantId(
                viewer.eventId(),
                viewer.participantType(),
                participantIdValue(viewer)
        );
        return Map.of("totalConnections", total);
    }

    @Transactional
    public Map<String, Object> likeParticipant(DatingParticipant viewer, String targetParticipantType, Long targetParticipantId) {
        requireCheckedInSignup(viewer);
        String eventId = viewer.eventId();
        requireDatingEvent(eventId);
        String normalizedType = normalizeParticipantType(targetParticipantType);
        if (targetParticipantId == null || targetParticipantId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "目标参与者无效");
        }
        DatingParticipant.ParsedParticipantKey parsed = new DatingParticipant.ParsedParticipantKey(normalizedType, targetParticipantId);
        DatingParticipant target = resolveParticipant(eventId, parsed);
        if (isSameParticipant(viewer, target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能喜欢自己");
        }
        requireRosterVisible(eventId, target);

        Optional<DatingLike> existing = likeRepository
                .findByEventIdAndParticipantTypeAndParticipantIdAndTargetParticipantTypeAndTargetParticipantId(
                        eventId,
                        viewer.participantType(),
                        participantIdValue(viewer),
                        target.participantType(),
                        participantIdValue(target)
                );
        if (existing.isPresent()) {
            return Map.of("success", true);
        }

        DatingLike like = new DatingLike();
        like.setEventId(eventId);
        like.setParticipantType(viewer.participantType());
        like.setParticipantId(participantIdValue(viewer));
        like.setTargetParticipantType(target.participantType());
        like.setTargetParticipantId(participantIdValue(target));
        likeRepository.save(like);
        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Object> unlikeParticipant(DatingParticipant viewer, Long likeId) {
        requireCheckedInSignup(viewer);
        requireDatingEvent(viewer.eventId());
        DatingLike like = likeRepository
                .findByIdAndEventIdAndParticipantTypeAndParticipantId(
                        likeId,
                        viewer.eventId(),
                        viewer.participantType(),
                        participantIdValue(viewer)
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "喜欢记录不存在"));
        likeRepository.delete(like);
        return Map.of("success", true);
    }

    public List<Map<String, Object>> listMyLikes(DatingParticipant viewer) {
        requireCheckedInSignup(viewer);
        requireDatingEvent(viewer.eventId());
        return likeRepository
                .findByEventIdAndParticipantTypeAndParticipantIdOrderByCreatedAtDesc(
                        viewer.eventId(),
                        viewer.participantType(),
                        participantIdValue(viewer)
                )
                .stream()
                .map(like -> toLikeListItem(viewer.eventId(), like))
                .toList();
    }

    public Map<String, Object> getLikeStats(DatingParticipant viewer) {
        requireCheckedInSignup(viewer);
        requireDatingEvent(viewer.eventId());
        long total = likeRepository.countByEventIdAndParticipantTypeAndParticipantId(
                viewer.eventId(),
                viewer.participantType(),
                participantIdValue(viewer)
        );
        return Map.of("totalLikes", total);
    }

    @Transactional
    public Map<String, Object> recomputeMutualMatches(String eventId) {
        Map<String, Object> result = recomputeMutualMatchesInternal(eventId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> createdMatches = (List<Map<String, Object>>) result.get("createdMatches");
        mutualMatchNotifyService.notifyNewMatches(eventId, createdMatches);
        return Map.of(
                "success", true,
                "created", result.get("createdMatchCount")
        );
    }

    @Transactional
    public Map<String, Object> recomputeMutualMatchesInternal(String eventId) {
        PlatformEvent event = requireDatingEvent(eventId);
        if (!PlatformEventLifecycle.isEnded(event, LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动尚未结束，无法计算互选结果");
        }
        return computeMutualMatches(eventId);
    }

    public Object listMyMutualMatches(DatingParticipant viewer) {
        requireCheckedInSignup(viewer);
        PlatformEvent event = requireDatingEvent(viewer.eventId());
        if (!PlatformEventLifecycle.isEnded(event, LocalDateTime.now())) {
            Map<String, Object> pending = new LinkedHashMap<>();
            pending.put("eventEnded", false);
            pending.put("message", "活动结束后可查看互选结果");
            return pending;
        }
        computeMutualMatches(viewer.eventId());
        return listMutualMatchesForParticipant(viewer);
    }

    public Map<String, Object> getAdminDatingStats(String eventId) {
        requireDatingEvent(eventId);
        long checkedInCount = signupRepository.countByEventIdAndCheckedInTrue(eventId);
        long connectionCount = connectionRepository.countByEventId(eventId);
        long likeCount = likeRepository.countByEventId(eventId);
        long mutualMatchCount = mutualMatchRepository.countByEventId(eventId);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("checkedInCount", checkedInCount);
        stats.put("connectionCount", connectionCount);
        stats.put("likeCount", likeCount);
        stats.put("mutualMatchCount", mutualMatchCount);
        return stats;
    }

    public Map<String, Object> getRoster(DatingParticipant participant) {
        requireCheckedInSignup(participant);
        String eventId = participant.eventId();
        List<EventSignup> checkedIns = signupRepository.findByEventIdAndCheckedInTrue(eventId);
        if (checkedIns.isEmpty()) {
            return Map.of("male", List.of(), "female", List.of(), "other", List.of());
        }

        Set<Long> checkedInUserIds = checkedIns.stream()
                .map(EventSignup::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> checkedInGuestIds = checkedIns.stream()
                .map(EventSignup::getGuestParticipantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<DatingEventIdentity> identities = identityRepository.findByEventIdOrderByGenderSideAscBadgeSeqAsc(eventId)
                .stream()
                .filter(i -> (i.getUserId() != null && checkedInUserIds.contains(i.getUserId()))
                        || (i.getGuestParticipantId() != null && checkedInGuestIds.contains(i.getGuestParticipantId())))
                .toList();

        Map<Long, DatingEventProfile> userProfileMap = profileRepository
                .findByEventIdAndUserIdIn(eventId, new ArrayList<>(checkedInUserIds))
                .stream()
                .collect(Collectors.toMap(DatingEventProfile::getUserId, p -> p, (a, b) -> a));
        Map<Long, DatingEventProfile> guestProfileMap = profileRepository
                .findByEventIdAndGuestParticipantIdIn(eventId, new ArrayList<>(checkedInGuestIds))
                .stream()
                .collect(Collectors.toMap(DatingEventProfile::getGuestParticipantId, p -> p, (a, b) -> a));

        Set<String> likedKeys = likeRepository
                .findByEventIdAndParticipantTypeAndParticipantIdOrderByCreatedAtDesc(
                        eventId,
                        participant.participantType(),
                        participantIdValue(participant)
                )
                .stream()
                .map(like -> like.getTargetParticipantType() + ":" + like.getTargetParticipantId())
                .collect(Collectors.toSet());

        Map<Long, String> photoByUserId = unifiedProfileService.getPrimaryPhotoUrlByUserIds(checkedInUserIds);

        List<Map<String, Object>> male = new ArrayList<>();
        List<Map<String, Object>> female = new ArrayList<>();
        List<Map<String, Object>> other = new ArrayList<>();
        identities.stream()
                .sorted((a, b) -> {
                    int side = a.getGenderSide().compareTo(b.getGenderSide());
                    return side != 0 ? side : Integer.compare(a.getBadgeSeq(), b.getBadgeSeq());
                })
                .forEach(identity -> {
                    DatingEventProfile profile = identity.getUserId() != null
                            ? userProfileMap.get(identity.getUserId())
                            : guestProfileMap.get(identity.getGuestParticipantId());
                    String photoUrl = identity.getUserId() != null
                            ? photoByUserId.get(identity.getUserId())
                            : null;
                    Map<String, Object> row = toRosterRow(identity, profile, photoUrl);
                    String participantKey = String.valueOf(row.get("participantKey"));
                    row.put("liked", likedKeys.contains(participantKey));
                    if (DatingEventTemplate.SIDE_FEMALE.equals(identity.getGenderSide())) {
                        female.add(row);
                    } else if (DatingEventTemplate.SIDE_OTHER.equals(identity.getGenderSide())) {
                        other.add(row);
                    } else {
                        male.add(row);
                    }
                });
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("male", male);
        result.put("female", female);
        result.put("other", other);
        return result;
    }

    public Optional<DatingEventIdentity> findIdentity(String eventId, Long userId) {
        return identityRepository.findByEventIdAndUserId(eventId, userId);
    }

    public void enrichSignupRow(Map<String, Object> row, PlatformEvent event, Long userId) {
        String templateType = event.getTemplateType() == null
                ? DatingEventTemplate.TYPE_GENERAL
                : event.getTemplateType();
        row.put("templateType", templateType);
        if (!isDatingEvent(event)) {
            return;
        }
        identityRepository.findByEventIdAndUserId(event.getId(), userId)
                .ifPresent(identity -> row.put("badgeLabel", identity.getBadgeLabel()));
        boolean profileCompleted = profileRepository.findByEventIdAndUserId(event.getId(), userId)
                .map(DatingEventProfile::getCompleted)
                .orElse(false);
        row.put("datingProfileCompleted", profileCompleted);
    }

    private Map<String, Object> buildCard(DatingParticipant target, DatingParticipant viewer) {
        String eventId = target.eventId();
        DatingEventIdentity identity = findIdentity(target)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "该参与者尚未分配活动编号"));
        DatingEventProfile profile = findProfile(target).orElse(null);
        boolean isSelf = isSameParticipant(target, viewer);
        if (!isSelf && (profile == null || !Boolean.TRUE.equals(profile.getCompleted()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "对方尚未完善本场活动资料");
        }
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("eventId", eventId);
        card.put("participantKey", target.participantKey());
        card.put("participantType", target.participantType());
        if (target.userId() != null) {
            card.put("userId", target.userId());
        }
        if (target.guestParticipantId() != null) {
            card.put("guestParticipantId", target.guestParticipantId());
        }
        card.put("badgeLabel", identity.getBadgeLabel());
        card.put("genderSide", identity.getGenderSide());
        card.put("nickname", target.nickname());
        card.put("completed", profile != null && Boolean.TRUE.equals(profile.getCompleted()));
        if (profile != null) {
            card.put("age", profile.getAge());
            card.put("heightCm", profile.getHeightCm());
            card.put("city", profile.getCity());
            card.put("occupation", profile.getOccupation());
            card.put("education", profile.getEducation());
            card.put("interestTags", deserializeTags(profile.getInterestTags()));
            card.put("selfIntro", profile.getSelfIntro());
            card.put("partnerRequirements", profile.getPartnerRequirements());
            card.put("idealPartnerDesc", profile.getIdealPartnerDesc());
        }
        card.put("isSelf", isSelf);
        if (!isSelf) {
            card.put("connected", hasConnection(viewer, target));
            card.put("liked", hasLike(viewer, target));
        }
        return card;
    }

    private boolean hasConnection(DatingParticipant viewer, DatingParticipant target) {
        return connectionRepository
                .findByEventIdAndParticipantTypeAndParticipantIdAndTargetParticipantTypeAndTargetParticipantId(
                        viewer.eventId(),
                        viewer.participantType(),
                        participantIdValue(viewer),
                        target.participantType(),
                        participantIdValue(target)
                )
                .isPresent();
    }

    private boolean hasLike(DatingParticipant viewer, DatingParticipant target) {
        return likeRepository
                .findByEventIdAndParticipantTypeAndParticipantIdAndTargetParticipantTypeAndTargetParticipantId(
                        viewer.eventId(),
                        viewer.participantType(),
                        participantIdValue(viewer),
                        target.participantType(),
                        participantIdValue(target)
                )
                .isPresent();
    }

    private Map<String, Object> computeMutualMatches(String eventId) {
        List<DatingLike> likes = likeRepository.findByEventId(eventId);
        Map<String, Set<String>> likeMap = new LinkedHashMap<>();
        for (DatingLike like : likes) {
            String fromKey = like.getParticipantType() + ":" + like.getParticipantId();
            String toKey = like.getTargetParticipantType() + ":" + like.getTargetParticipantId();
            likeMap.computeIfAbsent(fromKey, key -> new java.util.HashSet<>()).add(toKey);
        }

        int createdCount = 0;
        List<Map<String, Object>> createdMatches = new ArrayList<>();
        Set<String> processed = new java.util.HashSet<>();
        for (DatingLike like : likes) {
            String fromKey = like.getParticipantType() + ":" + like.getParticipantId();
            String toKey = like.getTargetParticipantType() + ":" + like.getTargetParticipantId();
            Set<String> reverseLikes = likeMap.get(toKey);
            if (reverseLikes == null || !reverseLikes.contains(fromKey)) {
                continue;
            }
            String pairKey = canonicalPairKey(
                    like.getParticipantType(),
                    like.getParticipantId(),
                    like.getTargetParticipantType(),
                    like.getTargetParticipantId()
            );
            if (!processed.add(pairKey)) {
                continue;
            }
            NormalizedPair pair = normalizePair(
                    like.getParticipantType(),
                    like.getParticipantId(),
                    like.getTargetParticipantType(),
                    like.getTargetParticipantId()
            );
            if (mutualMatchRepository
                    .findByEventIdAndParticipantATypeAndParticipantAIdAndParticipantBTypeAndParticipantBId(
                            eventId,
                            pair.typeA(),
                            pair.idA(),
                            pair.typeB(),
                            pair.idB()
                    )
                    .isPresent()) {
                continue;
            }
            DatingMutualMatch match = new DatingMutualMatch();
            match.setEventId(eventId);
            match.setParticipantAType(pair.typeA());
            match.setParticipantAId(pair.idA());
            match.setParticipantBType(pair.typeB());
            match.setParticipantBId(pair.idB());
            match.setMatchedAt(LocalDateTime.now());
            mutualMatchRepository.save(match);
            createdCount++;
            createdMatches.add(toCreatedMatchMap(eventId, match));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("createdMatchCount", createdCount);
        result.put("createdMatches", createdMatches);
        return result;
    }

    private Map<String, Object> toCreatedMatchMap(String eventId, DatingMutualMatch match) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("matchId", match.getId());
        row.put("eventId", eventId);
        row.put("participantA", Map.of(
                "type", match.getParticipantAType(),
                "id", match.getParticipantAId()
        ));
        row.put("participantB", Map.of(
                "type", match.getParticipantBType(),
                "id", match.getParticipantBId()
        ));
        return row;
    }

    private List<Map<String, Object>> listMutualMatchesForParticipant(DatingParticipant viewer) {
        String eventId = viewer.eventId();
        String type = viewer.participantType();
        long id = participantIdValue(viewer);

        List<DatingMutualMatch> asA = mutualMatchRepository.findByEventIdAndParticipantATypeAndParticipantAId(eventId, type, id);
        List<DatingMutualMatch> asB = mutualMatchRepository.findByEventIdAndParticipantBTypeAndParticipantBId(eventId, type, id);

        List<Map<String, Object>> result = new ArrayList<>();
        for (DatingMutualMatch match : asA) {
            result.add(toMutualMatchListItem(eventId, match, false));
        }
        for (DatingMutualMatch match : asB) {
            result.add(toMutualMatchListItem(eventId, match, true));
        }
        result.sort((a, b) -> {
            LocalDateTime ta = (LocalDateTime) a.get("matchedAt");
            LocalDateTime tb = (LocalDateTime) b.get("matchedAt");
            if (ta == null && tb == null) {
                return 0;
            }
            if (ta == null) {
                return 1;
            }
            if (tb == null) {
                return -1;
            }
            return tb.compareTo(ta);
        });
        return result;
    }

    private Map<String, Object> toLikeListItem(String eventId, DatingLike like) {
        DatingParticipant.ParsedParticipantKey parsed = new DatingParticipant.ParsedParticipantKey(
                like.getTargetParticipantType(),
                like.getTargetParticipantId()
        );
        DatingParticipant target = resolveParticipant(eventId, parsed);
        DatingEventIdentity identity = findIdentity(target)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "参与者不存在"));
        DatingEventProfile profile = findProfile(target).orElse(null);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("likeId", like.getId());
        row.put("participantKey", target.participantKey());
        row.put("badgeLabel", identity.getBadgeLabel());
        row.put("nickname", target.nickname());
        if (profile != null) {
            row.put("age", profile.getAge());
            row.put("city", profile.getCity());
            row.put("occupation", profile.getOccupation());
        }
        row.put("avatar", resolveAvatar(target));
        row.put("createdAt", like.getCreatedAt());
        return row;
    }

    private Map<String, Object> toMutualMatchListItem(String eventId, DatingMutualMatch match, boolean viewerIsB) {
        String targetType = viewerIsB ? match.getParticipantAType() : match.getParticipantBType();
        Long targetId = viewerIsB ? match.getParticipantAId() : match.getParticipantBId();
        DatingParticipant.ParsedParticipantKey parsed = new DatingParticipant.ParsedParticipantKey(targetType, targetId);
        DatingParticipant target = resolveParticipant(eventId, parsed);
        DatingEventIdentity identity = findIdentity(target)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "参与者不存在"));
        DatingEventProfile profile = findProfile(target).orElse(null);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("matchId", match.getId());
        row.put("participantKey", target.participantKey());
        row.put("badgeLabel", identity.getBadgeLabel());
        row.put("nickname", target.nickname());
        if (profile != null) {
            row.put("age", profile.getAge());
            row.put("city", profile.getCity());
            row.put("occupation", profile.getOccupation());
        }
        row.put("avatar", resolveAvatar(target));
        row.put("matchedAt", match.getMatchedAt());
        return row;
    }

    private String canonicalPairKey(String typeA, Long idA, String typeB, Long idB) {
        NormalizedPair pair = normalizePair(typeA, idA, typeB, idB);
        return pair.typeA() + ":" + pair.idA() + "|" + pair.typeB() + ":" + pair.idB();
    }

    private NormalizedPair normalizePair(String typeA, Long idA, String typeB, Long idB) {
        String keyA = typeA + ":" + idA;
        String keyB = typeB + ":" + idB;
        if (keyA.compareTo(keyB) <= 0) {
            return new NormalizedPair(typeA, idA, typeB, idB);
        }
        return new NormalizedPair(typeB, idB, typeA, idA);
    }

    private record NormalizedPair(String typeA, Long idA, String typeB, Long idB) {
    }

    private Map<String, Object> toConnectionListItem(String eventId, DatingConnection connection) {
        DatingParticipant.ParsedParticipantKey parsed = new DatingParticipant.ParsedParticipantKey(
                connection.getTargetParticipantType(),
                connection.getTargetParticipantId()
        );
        DatingParticipant target = resolveParticipant(eventId, parsed);
        DatingEventIdentity identity = findIdentity(target)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "参与者不存在"));
        DatingEventProfile profile = findProfile(target).orElse(null);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("participantKey", target.participantKey());
        row.put("badgeLabel", identity.getBadgeLabel());
        row.put("nickname", target.nickname());
        if (profile != null) {
            row.put("age", profile.getAge());
            row.put("city", profile.getCity());
            row.put("occupation", profile.getOccupation());
        }
        row.put("avatar", resolveAvatar(target));
        row.put("createdAt", connection.getCreatedAt());
        return row;
    }

    private String resolveAvatar(DatingParticipant participant) {
        if (participant.isGuest()) {
            return null;
        }
        User user = loadUser(participant.userId());
        return user.getProfilePhoto();
    }

    private long participantIdValue(DatingParticipant participant) {
        if (participant.isGuest()) {
            return participant.guestParticipantId();
        }
        return participant.userId();
    }

    private String normalizeParticipantType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "目标参与者类型无效");
        }
        String normalized = raw.trim().toUpperCase();
        if (!DatingParticipant.TYPE_REGISTERED.equals(normalized) && !DatingParticipant.TYPE_GUEST.equals(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "目标参与者类型无效");
        }
        return normalized;
    }

    private Map<String, Object> toRosterRow(DatingEventIdentity identity, DatingEventProfile profile, String photoUrl) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (identity.getUserId() != null) {
            row.put("userId", identity.getUserId());
            row.put("participantKey", DatingParticipant.TYPE_REGISTERED + ":" + identity.getUserId());
            row.put("participantType", DatingParticipant.TYPE_REGISTERED);
        } else if (identity.getGuestParticipantId() != null) {
            row.put("guestParticipantId", identity.getGuestParticipantId());
            row.put("participantKey", DatingParticipant.TYPE_GUEST + ":" + identity.getGuestParticipantId());
            row.put("participantType", DatingParticipant.TYPE_GUEST);
            guestRepository.findById(identity.getGuestParticipantId())
                    .ifPresent(g -> row.put("nickname", g.getNickname()));
        }
        row.put("badgeLabel", identity.getBadgeLabel());
        row.put("genderSide", identity.getGenderSide());
        boolean completed = profile != null && Boolean.TRUE.equals(profile.getCompleted());
        row.put("profileCompleted", completed);
        if (completed && profile != null) {
            row.put("age", profile.getAge());
            row.put("city", profile.getCity());
            row.put("occupation", profile.getOccupation());
        }
        if (photoUrl != null && !photoUrl.isBlank()) {
            row.put("photo", photoUrl);
        }
        return row;
    }

    private void requireRosterVisible(String eventId, DatingParticipant target) {
        EventSignup signup = findSignup(target)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "参与者不存在"));
        if (!Boolean.TRUE.equals(signup.getCheckedIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该参与者尚未签到");
        }
        if (findIdentity(target).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该参与者尚未分配活动编号");
        }
    }

    private void requireCheckedInSignup(DatingParticipant participant) {
        EventSignup signup = findSignup(participant)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先报名活动"));
        if (!Boolean.TRUE.equals(signup.getCheckedIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先完成现场签到");
        }
    }

    private DatingEventIdentity assignIdentity(DatingParticipant participant, String genderSide) {
        String eventId = participant.eventId();
        int nextSeq = identityRepository.findMaxBadgeSeq(eventId, genderSide) + 1;
        String prefix = badgePrefix(genderSide);
        String badgeLabel = prefix + String.format("%0" + BADGE_PAD + "d", nextSeq);

        DatingEventIdentity identity = new DatingEventIdentity();
        identity.setEventId(eventId);
        if (participant.isGuest()) {
            identity.setGuestParticipantId(participant.guestParticipantId());
        } else {
            identity.setUserId(participant.userId());
        }
        identity.setGenderSide(genderSide);
        identity.setBadgeSeq(nextSeq);
        identity.setBadgeLabel(badgeLabel);
        identity.setAssignedAt(LocalDateTime.now());
        return identityRepository.save(identity);
    }

    private String badgePrefix(String genderSide) {
        if (DatingEventTemplate.SIDE_FEMALE.equals(genderSide)) {
            return "女";
        }
        if (DatingEventTemplate.SIDE_OTHER.equals(genderSide)) {
            return "嘉宾";
        }
        return "男";
    }

    private Optional<EventSignup> findSignup(DatingParticipant participant) {
        if (participant.isGuest()) {
            return signupRepository.findByEventIdAndGuestParticipantId(participant.eventId(), participant.guestParticipantId());
        }
        return signupRepository.findByEventIdAndUserId(participant.eventId(), participant.userId());
    }

    private Optional<DatingEventIdentity> findIdentity(DatingParticipant participant) {
        if (participant.isGuest()) {
            return identityRepository.findByEventIdAndGuestParticipantId(participant.eventId(), participant.guestParticipantId());
        }
        return identityRepository.findByEventIdAndUserId(participant.eventId(), participant.userId());
    }

    private Optional<DatingEventProfile> findProfile(DatingParticipant participant) {
        if (participant.isGuest()) {
            return profileRepository.findByEventIdAndGuestParticipantId(participant.eventId(), participant.guestParticipantId());
        }
        return profileRepository.findByEventIdAndUserId(participant.eventId(), participant.userId());
    }

    private DatingParticipant resolveParticipant(String eventId, DatingParticipant.ParsedParticipantKey parsed) {
        if (parsed.isGuest()) {
            EventGuestParticipant guest = guestRepository.findById(parsed.id())
                    .filter(g -> eventId.equals(g.getEventId()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "参与者不存在"));
            return DatingParticipant.fromGuest(guest);
        }
        User user = loadUser(parsed.id());
        return DatingParticipant.fromUser(user, eventId);
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    private Map<String, Object> guestProfilePrefill(DatingParticipant participant) {
        Map<String, Object> prefill = new LinkedHashMap<>();
        prefill.put("genderSide", participant.genderSide());
        prefill.put("nickname", participant.nickname());
        prefill.put("interestTags", List.of());
        return prefill;
    }

    private boolean isSameParticipant(DatingParticipant a, DatingParticipant b) {
        if (a.isGuest() && b.isGuest()) {
            return Objects.equals(a.guestParticipantId(), b.guestParticipantId());
        }
        if (!a.isGuest() && !b.isGuest()) {
            return Objects.equals(a.userId(), b.userId());
        }
        return false;
    }

    private String resolveGenderSide(DatingParticipant participant) {
        if (participant.genderSide() != null && !participant.genderSide().isBlank()) {
            return normalizeGenderSide(participant.genderSide());
        }
        if (!participant.isGuest()) {
            return resolveGenderSide(loadUser(participant.userId()));
        }
        return null;
    }

    private String resolveGenderSide(User user) {
        if (user.getGender() != null) {
            if (user.getGender() == 1) {
                return DatingEventTemplate.SIDE_MALE;
            }
            if (user.getGender() == 2) {
                return DatingEventTemplate.SIDE_FEMALE;
            }
            return DatingEventTemplate.SIDE_OTHER;
        }
        Map<String, Object> fellowship = unifiedProfileService.buildFellowshipPayload(user);
        return mapGenderToSide((String) fellowship.get("gender"));
    }

    private String mapGenderToSide(String gender) {
        if (gender == null || gender.isBlank()) {
            return null;
        }
        if ("female".equalsIgnoreCase(gender) || "女".equals(gender) || "2".equals(gender)) {
            return DatingEventTemplate.SIDE_FEMALE;
        }
        if ("male".equalsIgnoreCase(gender) || "男".equals(gender) || "1".equals(gender)) {
            return DatingEventTemplate.SIDE_MALE;
        }
        return null;
    }

    private String normalizeGenderSide(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String v = raw.trim().toUpperCase();
        if (DatingEventTemplate.SIDE_MALE.equals(v) || "男".equals(raw) || "M".equals(v)) {
            return DatingEventTemplate.SIDE_MALE;
        }
        if (DatingEventTemplate.SIDE_FEMALE.equals(v) || "女".equals(raw) || "F".equals(v)) {
            return DatingEventTemplate.SIDE_FEMALE;
        }
        if (DatingEventTemplate.SIDE_OTHER.equals(v) || "其他".equals(raw) || "O".equals(v)) {
            return DatingEventTemplate.SIDE_OTHER;
        }
        return mapGenderToSide(raw);
    }

    private boolean isProfileComplete(DatingEventProfile profile) {
        return profile.getAge() != null
                && profile.getCity() != null && !profile.getCity().isBlank()
                && profile.getSelfIntro() != null && !profile.getSelfIntro().isBlank();
    }

    private Map<String, Object> toIdentityMap(DatingEventIdentity identity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("assigned", true);
        map.put("eventId", identity.getEventId());
        if (identity.getUserId() != null) {
            map.put("userId", identity.getUserId());
            map.put("participantKey", DatingParticipant.TYPE_REGISTERED + ":" + identity.getUserId());
        }
        if (identity.getGuestParticipantId() != null) {
            map.put("guestParticipantId", identity.getGuestParticipantId());
            map.put("participantKey", DatingParticipant.TYPE_GUEST + ":" + identity.getGuestParticipantId());
        }
        map.put("genderSide", identity.getGenderSide());
        map.put("badgeSeq", identity.getBadgeSeq());
        map.put("badgeLabel", identity.getBadgeLabel());
        map.put("assignedAt", identity.getAssignedAt());
        return map;
    }

    private Map<String, Object> toProfileMap(DatingEventProfile profile) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("saved", true);
        map.put("age", profile.getAge());
        map.put("heightCm", profile.getHeightCm());
        map.put("city", profile.getCity());
        map.put("occupation", profile.getOccupation());
        map.put("education", profile.getEducation());
        map.put("interestTags", deserializeTags(profile.getInterestTags()));
        map.put("selfIntro", profile.getSelfIntro());
        map.put("partnerRequirements", profile.getPartnerRequirements());
        map.put("idealPartnerDesc", profile.getIdealPartnerDesc());
        map.put("completed", Boolean.TRUE.equals(profile.getCompleted()));
        map.put("updatedAt", profile.getUpdatedAt());
        return map;
    }

    private List<String> deserializeTags(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {});
        } catch (Exception ignored) {
            return List.of(raw.split("[,，]")).stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
    }

    private String serializeTags(Object raw) {
        if (raw == null) {
            return "[]";
        }
        try {
            if (raw instanceof List<?> list) {
                return objectMapper.writeValueAsString(list);
            }
            if (raw instanceof String s) {
                if (s.isBlank()) {
                    return "[]";
                }
                return objectMapper.writeValueAsString(
                        List.of(s.split("[,，]")).stream().map(String::trim).filter(x -> !x.isEmpty()).toList()
                );
            }
        } catch (Exception ignored) {
        }
        return "[]";
    }

    private List<String> parseTagsString(Object raw) {
        if (raw == null) {
            return List.of();
        }
        String s = String.valueOf(raw).trim();
        if (s.isEmpty()) {
            return List.of();
        }
        if (s.startsWith("[")) {
            return deserializeTags(s);
        }
        return List.of(s.split("[,，]")).stream().map(String::trim).filter(x -> !x.isEmpty()).toList();
    }

    private Integer intVal(Object raw) {
        if (raw == null || String.valueOf(raw).isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String stringVal(Object raw) {
        return raw == null ? "" : String.valueOf(raw).trim();
    }

    private String trim(Object raw, int maxLen) {
        if (raw == null) {
            return null;
        }
        String s = String.valueOf(raw).trim();
        if (s.isEmpty()) {
            return null;
        }
        return s.length() > maxLen ? s.substring(0, maxLen) : s;
    }
}
