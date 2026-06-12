package com.lovecube.backend.controllers;



import com.lovecube.backend.dating.DatingParticipant;

import com.lovecube.backend.models.User;

import com.lovecube.backend.services.AdminAuthService;

import com.lovecube.backend.services.DatingEventService;

import com.lovecube.backend.services.DatingParticipantAuthService;

import org.springframework.web.bind.annotation.*;



import java.util.List;
import java.util.Map;



@RestController

@RequestMapping("/api/events/{eventId}/dating")

public class DatingEventController {



    private final AdminAuthService adminAuthService;

    private final DatingEventService datingEventService;

    private final DatingParticipantAuthService participantAuthService;



    public DatingEventController(

            AdminAuthService adminAuthService,

            DatingEventService datingEventService,

            DatingParticipantAuthService participantAuthService

    ) {

        this.adminAuthService = adminAuthService;

        this.datingEventService = datingEventService;

        this.participantAuthService = participantAuthService;

    }



    @GetMapping("/context")

    public Map<String, Object> getContext(

            @PathVariable String eventId,

            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken

    ) {

        DatingParticipant participant = participantAuthService.resolve(eventId, authHeader, guestToken);

        return datingEventService.getContext(participant);

    }



    @GetMapping("/me/identity")

    public Map<String, Object> getMyIdentity(

            @PathVariable String eventId,

            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken

    ) {

        DatingParticipant participant = participantAuthService.resolve(eventId, authHeader, guestToken);

        return datingEventService.getMyIdentity(participant);

    }



    @GetMapping("/me/profile/prefill")

    public Map<String, Object> getProfilePrefill(

            @PathVariable String eventId,

            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken

    ) {

        DatingParticipant participant = participantAuthService.resolve(eventId, authHeader, guestToken);

        datingEventService.requireDatingEvent(eventId);

        if (participant.isGuest()) {

            return Map.of(

                    "genderSide", participant.genderSide(),

                    "nickname", participant.nickname(),

                    "interestTags", java.util.List.of()

            );

        }

        User user = adminAuthService.requireUser(authHeader);

        return datingEventService.getProfilePrefill(user);

    }



    @GetMapping("/me/profile")

    public Map<String, Object> getMyProfile(

            @PathVariable String eventId,

            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken

    ) {

        DatingParticipant participant = participantAuthService.resolve(eventId, authHeader, guestToken);

        return datingEventService.getMyProfile(participant);

    }



    @PutMapping("/me/profile")

    public Map<String, Object> saveMyProfile(

            @PathVariable String eventId,

            @RequestBody Map<String, Object> body,

            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken

    ) {

        DatingParticipant participant = participantAuthService.resolve(eventId, authHeader, guestToken);

        return datingEventService.saveMyProfile(participant, body);

    }



    @GetMapping("/me/card")

    public Map<String, Object> getMyCard(

            @PathVariable String eventId,

            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken

    ) {

        DatingParticipant participant = participantAuthService.resolve(eventId, authHeader, guestToken);

        return datingEventService.getMyCard(participant);

    }



    @GetMapping("/roster")

    public Map<String, Object> getRoster(

            @PathVariable String eventId,

            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken

    ) {

        DatingParticipant participant = participantAuthService.resolve(eventId, authHeader, guestToken);

        return datingEventService.getRoster(participant);

    }



    @GetMapping("/roster/{targetUserId}/card")

    public Map<String, Object> getUserCard(

            @PathVariable String eventId,

            @PathVariable Long targetUserId,

            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken

    ) {

        DatingParticipant viewer = participantAuthService.resolve(eventId, authHeader, guestToken);

        return datingEventService.getParticipantCard(viewer, DatingParticipant.TYPE_REGISTERED + ":" + targetUserId);

    }



    @GetMapping("/roster/participant/{participantKey}/card")

    public Map<String, Object> getParticipantCard(

            @PathVariable String eventId,

            @PathVariable String participantKey,

            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken

    ) {

        DatingParticipant viewer = participantAuthService.resolve(eventId, authHeader, guestToken);

        return datingEventService.getParticipantCard(viewer, participantKey);

    }

    @PostMapping("/connections")
    public Map<String, Object> createConnection(
            @PathVariable String eventId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken
    ) {
        DatingParticipant viewer = participantAuthService.resolve(eventId, authHeader, guestToken);
        String targetType = body != null ? String.valueOf(body.get("targetParticipantType")) : null;
        Long targetId = parseLong(body != null ? body.get("targetParticipantId") : null);
        return datingEventService.createConnection(viewer, targetType, targetId);
    }

    @DeleteMapping("/connections/{connectionId}")
    public Map<String, Object> deleteConnection(
            @PathVariable String eventId,
            @PathVariable Long connectionId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken
    ) {
        DatingParticipant viewer = participantAuthService.resolve(eventId, authHeader, guestToken);
        return datingEventService.deleteConnection(viewer, connectionId);
    }

    @GetMapping("/my-connections")
    public List<Map<String, Object>> listMyConnections(
            @PathVariable String eventId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken
    ) {
        DatingParticipant viewer = participantAuthService.resolve(eventId, authHeader, guestToken);
        return datingEventService.listMyConnections(viewer);
    }

    @GetMapping("/my-connections/stats")
    public Map<String, Object> connectionStats(
            @PathVariable String eventId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken
    ) {
        DatingParticipant viewer = participantAuthService.resolve(eventId, authHeader, guestToken);
        return datingEventService.getConnectionStats(viewer);
    }

    @PostMapping("/likes")
    public Map<String, Object> createLike(
            @PathVariable String eventId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken
    ) {
        DatingParticipant viewer = participantAuthService.resolve(eventId, authHeader, guestToken);
        String targetType = body != null ? String.valueOf(body.get("targetParticipantType")) : null;
        Long targetId = parseLong(body != null ? body.get("targetParticipantId") : null);
        return datingEventService.likeParticipant(viewer, targetType, targetId);
    }

    @DeleteMapping("/likes/{likeId}")
    public Map<String, Object> deleteLike(
            @PathVariable String eventId,
            @PathVariable Long likeId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken
    ) {
        DatingParticipant viewer = participantAuthService.resolve(eventId, authHeader, guestToken);
        return datingEventService.unlikeParticipant(viewer, likeId);
    }

    @GetMapping("/my-likes")
    public List<Map<String, Object>> listMyLikes(
            @PathVariable String eventId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken
    ) {
        DatingParticipant viewer = participantAuthService.resolve(eventId, authHeader, guestToken);
        return datingEventService.listMyLikes(viewer);
    }

    @GetMapping("/my-likes/stats")
    public Map<String, Object> likeStats(
            @PathVariable String eventId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken
    ) {
        DatingParticipant viewer = participantAuthService.resolve(eventId, authHeader, guestToken);
        return datingEventService.getLikeStats(viewer);
    }

    @GetMapping("/my-mutual-matches")
    public Object listMyMutualMatches(
            @PathVariable String eventId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken
    ) {
        DatingParticipant viewer = participantAuthService.resolve(eventId, authHeader, guestToken);
        return datingEventService.listMyMutualMatches(viewer);
    }

    private Long parseLong(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(raw).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}

