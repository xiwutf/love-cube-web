package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.PlatGroup;
import com.lovecube.backend.entity.PlatGroupMember;
import com.lovecube.backend.entity.PlatGroupNotice;
import com.lovecube.backend.entity.PlatGroupPost;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.PlatGroupMemberRepository;
import com.lovecube.backend.repository.PlatGroupNoticeRepository;
import com.lovecube.backend.repository.PlatGroupPostRepository;
import com.lovecube.backend.repository.PlatGroupRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platform/groups")
public class PlatformGroupController {

    private static final Map<String, String> TYPE_LABELS = Map.of(
            "region", "地区团体",
            "church", "教会团体",
            "study", "学习小组",
            "interest", "兴趣团体",
            "family", "生活团契",
            "service", "事工团队"
    );

    private final PlatGroupRepository groupRepository;
    private final PlatGroupMemberRepository memberRepository;
    private final PlatGroupPostRepository postRepository;
    private final PlatGroupNoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;

    public PlatformGroupController(
            PlatGroupRepository groupRepository,
            PlatGroupMemberRepository memberRepository,
            PlatGroupPostRepository postRepository,
            PlatGroupNoticeRepository noticeRepository,
            UserRepository userRepository,
            AdminAuthService adminAuthService) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.noticeRepository = noticeRepository;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?List 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?

    @GetMapping
    public List<Map<String, Object>> listGroups(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long currentUserId = resolveOptionalUserId(authHeader);
        List<PlatGroup> groups = "all".equals(status)
                ? groupRepository.findAllByOrderByMemberCountDescCreatedAtDesc()
                : groupRepository.findByStatusOrderByMemberCountDescCreatedAtDesc(
                        (status != null && !status.isBlank()) ? status : "published");

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            groups = groups.stream()
                    .filter(g -> g.getName().toLowerCase().contains(kw)
                            || (g.getDescription() != null && g.getDescription().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }
        if (region != null && !region.isBlank()) {
            groups = groups.stream().filter(g -> region.equals(g.getRegion())).collect(Collectors.toList());
        }
        if (type != null && !type.isBlank()) {
            groups = groups.stream().filter(g -> type.equals(g.getType())).collect(Collectors.toList());
        }
        if ("newest".equals(sort)) {
            groups = groups.stream()
                    .sorted(Comparator.comparing(PlatGroup::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }

        Map<Long, PlatGroupMember> memberMap = loadUserMemberMap(currentUserId);
        return groups.stream().map(g -> buildGroupSummary(g, memberMap)).collect(Collectors.toList());
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?Hot 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?

    @GetMapping("/hot")
    public List<Map<String, Object>> hotGroups(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long currentUserId = resolveOptionalUserId(authHeader);
        List<PlatGroup> groups = groupRepository.findTop5ByStatusOrderByMemberCountDesc("published");
        Map<Long, PlatGroupMember> memberMap = loadUserMemberMap(currentUserId);
        return groups.stream().map(g -> buildGroupSummary(g, memberMap)).collect(Collectors.toList());
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?Feed 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾?

    @GetMapping("/feed")
    public List<Map<String, Object>> feed() {
        List<PlatGroupPost> posts = postRepository.findTop20ByStatusOrderByCreatedAtDesc("published");

        Set<Long> groupIds = posts.stream().map(PlatGroupPost::getGroupId).collect(Collectors.toSet());
        Map<Long, PlatGroup> groupMap = groupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(PlatGroup::getId, g -> g));

        Set<Long> userIds = posts.stream().map(PlatGroupPost::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));

        return posts.stream().map(p -> {
            PlatGroup g = groupMap.get(p.getGroupId());
            User u = userMap.get(p.getUserId());
            String text = p.getContent().length() > 50
                    ? p.getContent().substring(0, 50) + "..."
                    : p.getContent();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("groupId", p.getGroupId());
            item.put("groupName", g != null ? g.getName() : "");
            item.put("avatarUrl", g != null ? g.getCoverUrl() : "");
            item.put("text", text);
            item.put("time", formatRelativeTime(p.getCreatedAt()));
            item.put("authorName", u != null ? u.getUsername() : "");
            item.put("content", p.getContent());
            item.put("createdAt", p.getCreatedAt());
            return item;
        }).collect(Collectors.toList());
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?My groups 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶?

    @GetMapping("/my")
    public List<Map<String, Object>> myGroups(
            @RequestHeader("Authorization") String authHeader) {

        User user = adminAuthService.requireUser(authHeader);
        List<PlatGroupMember> memberships = memberRepository.findByUserId(user.getUserid());

        List<PlatGroupMember> relevant = memberships.stream()
                .filter(m -> "approved".equals(m.getStatus()) || "pending".equals(m.getStatus()))
                .collect(Collectors.toList());

        if (relevant.isEmpty()) return Collections.emptyList();

        Set<Long> groupIds = relevant.stream().map(PlatGroupMember::getGroupId).collect(Collectors.toSet());
        Map<Long, PlatGroup> groupMap = groupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(PlatGroup::getId, g -> g));
        Map<Long, PlatGroupMember> memberMap = relevant.stream()
                .collect(Collectors.toMap(PlatGroupMember::getGroupId, m -> m, (a, b) -> a));

        return relevant.stream()
                .map(m -> {
                    PlatGroup group = groupMap.get(m.getGroupId());
                    if (group == null || !"published".equals(group.getStatus())) return null;
                    return buildGroupSummary(group, memberMap);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?Detail 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫?

    @GetMapping("/{idOrSlug}")
    public Map<String, Object> getGroup(
            @PathVariable String idOrSlug,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        PlatGroup group = resolveGroup(idOrSlug);
        Long currentUserId = resolveOptionalUserId(authHeader);
        Map<Long, PlatGroupMember> memberMap = loadUserMemberMap(currentUserId);
        Map<String, Object> result = buildGroupSummary(group, memberMap);

        // Include latest notice
        List<PlatGroupNotice> notices = noticeRepository
                .findByGroupIdAndStatusOrderByCreatedAtDesc(group.getId(), "published");
        if (!notices.isEmpty()) {
            PlatGroupNotice n = notices.get(0);
            Map<String, Object> notice = new LinkedHashMap<>();
            notice.put("id", n.getId());
            notice.put("title", n.getTitle());
            notice.put("content", n.getContent());
            notice.put("createdAt", n.getCreatedAt());
            result.put("latestNotice", notice);
        }

        // Include admins (owner + admin roles)
        List<PlatGroupMember> admins = memberRepository
                .findByGroupIdAndStatusOrderByJoinedAtAsc(group.getId(), "approved")
                .stream()
                .filter(m -> "owner".equals(m.getRole()) || "admin".equals(m.getRole()))
                .collect(Collectors.toList());
        Set<Long> adminUserIds = admins.stream().map(PlatGroupMember::getUserId).collect(Collectors.toSet());
        Map<Long, User> adminUsers = userRepository.findAllById(adminUserIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));
        result.put("admins", admins.stream().map(m -> {
            User u = adminUsers.get(m.getUserId());
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("userId", m.getUserId());
            a.put("role", m.getRole());
            a.put("name", u != null ? u.getUsername() : "");
            a.put("avatar", u != null ? u.getProfilePhoto() : "");
            return a;
        }).collect(Collectors.toList()));

        return result;
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?Create group 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾?


    @PostMapping
    @Transactional
    public Map<String, Object> createGroup(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {

        User user = adminAuthService.requireUser(authHeader);

        PlatGroup group = new PlatGroup();
        group.setName(String.valueOf(payload.getOrDefault("name", "")));
        group.setType(String.valueOf(payload.getOrDefault("type", "region")));
        group.setRegion(String.valueOf(payload.getOrDefault("region", "")));
        group.setDescription(String.valueOf(payload.getOrDefault("description", "")));
        group.setCoverUrl((String) payload.get("coverUrl"));
        group.setJoinMode(String.valueOf(payload.getOrDefault("joinMode", "audit")));
        group.setOwnerUserId(user.getUserid());
        group.setMemberCount(1);
        group.setStatus("published");
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());

        String slug = (String) payload.get("slug");
        group.setSlug((slug != null && !slug.isBlank()) ? slug : "group-" + System.currentTimeMillis());

        PlatGroup saved = groupRepository.save(group);

        PlatGroupMember owner = new PlatGroupMember();
        owner.setGroupId(saved.getId());
        owner.setUserId(user.getUserid());
        owner.setRole("owner");
        owner.setStatus("approved");
        owner.setJoinedAt(LocalDateTime.now());
        owner.setCreatedAt(LocalDateTime.now());
        owner.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(owner);

        return Map.of("id", saved.getId(), "slug", saved.getSlug(), "message", "Created successfully");
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?Update (admin) 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫?

    @PutMapping("/{id}")
    @Transactional
    public Map<String, Object> updateGroup(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload) {

        adminAuthService.requireAdmin(authHeader);
        PlatGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (payload.containsKey("name")) {
            String name = String.valueOf(payload.get("name")).trim();
            if (!name.isBlank()) group.setName(name);
        }
        String type = strOf(payload, "type");
        if (type == null) type = strOf(payload, "category");
        if (type != null && !type.isBlank()) group.setType(type);

        if (payload.containsKey("region")) group.setRegion(String.valueOf(payload.get("region")));
        if (payload.containsKey("description")) group.setDescription(String.valueOf(payload.get("description")));
        if (payload.containsKey("coverUrl")) group.setCoverUrl(String.valueOf(payload.get("coverUrl")));
        if (payload.containsKey("status")) group.setStatus(String.valueOf(payload.get("status")));

        if (payload.containsKey("joinMode")) {
            group.setJoinMode(String.valueOf(payload.get("joinMode")));
        } else if (payload.containsKey("joinType")) {
            group.setJoinMode("open".equals(String.valueOf(payload.get("joinType"))) ? "free" : "audit");
        }

        group.setUpdatedAt(LocalDateTime.now());
        PlatGroup saved = groupRepository.save(group);
        return buildGroupSummary(saved, Collections.emptyMap());
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?Join 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?

    @PostMapping("/{id}/join")
    @Transactional
    public Map<String, Object> joinGroup(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, Object> payload) {

        User user = adminAuthService.requireUser(authHeader);
        PlatGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (!"published".equals(group.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group is not joinable");
        }

        String reason = payload != null ? String.valueOf(payload.getOrDefault("message", "")).trim() : "";
        if (!"free".equals(group.getJoinMode()) && reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "申请验证信息不能为空");
        }
        if (reason.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "申请验证信息不能超过255个字符");
        }

        Optional<PlatGroupMember> existing = memberRepository.findByGroupIdAndUserId(id, user.getUserid());
        if (existing.isPresent()) {
            PlatGroupMember m = existing.get();
            if ("approved".equals(m.getStatus())) {
                return Map.of("joined", true, "pending", false, "message", "Joined successfully");
            }
            if ("pending".equals(m.getStatus())) {
                return Map.of("joined", false, "pending", true, "message", "Request pending");
            }
            // left or rejected 闂?allow re-join/re-apply
            if ("free".equals(group.getJoinMode())) {
                m.setStatus("approved");
                m.setJoinedAt(LocalDateTime.now());
                m.setUpdatedAt(LocalDateTime.now());
                memberRepository.save(m);
                group.setMemberCount((group.getMemberCount() == null ? 0 : group.getMemberCount()) + 1);
                groupRepository.save(group);
                return Map.of("joined", true, "pending", false, "message", "Joined successfully");
            }
            m.setStatus("pending");
            m.setApplyReason(reason);
            m.setUpdatedAt(LocalDateTime.now());
            memberRepository.save(m);
            return Map.of("joined", false, "pending", true, "message", "Request submitted, waiting for approval");
        }

        PlatGroupMember member = new PlatGroupMember();
        member.setGroupId(id);
        member.setUserId(user.getUserid());
        member.setRole("member");
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        if ("free".equals(group.getJoinMode())) {
            member.setStatus("approved");
            member.setJoinedAt(LocalDateTime.now());
            memberRepository.save(member);
            group.setMemberCount((group.getMemberCount() == null ? 0 : group.getMemberCount()) + 1);
            groupRepository.save(group);
            return Map.of("joined", true, "pending", false, "message", "Joined successfully");
        }

        member.setStatus("pending");
        member.setApplyReason(reason);
        memberRepository.save(member);
        return Map.of("joined", false, "pending", true, "message", "Request submitted, waiting for approval");
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?Leave 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾?

    @PostMapping("/{id}/leave")
    @Transactional
    public Map<String, Object> leaveGroup(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        User user = adminAuthService.requireUser(authHeader);
        PlatGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        PlatGroupMember member = memberRepository.findByGroupIdAndUserId(id, user.getUserid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not a group member"));

        if ("owner".equals(member.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner cannot leave group directly");
        }

        if ("approved".equals(member.getStatus())) {
            group.setMemberCount(Math.max(0, (group.getMemberCount() == null ? 0 : group.getMemberCount()) - 1));
            groupRepository.save(group);
        }

        member.setStatus("left");
        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        return Map.of("left", true, "message", "Left group successfully");
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?Members 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻?

    @GetMapping("/{id}/members")
    public List<Map<String, Object>> getMembers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "approved") String status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        List<PlatGroupMember> members = "all".equals(status)
                ? memberRepository.findByGroupIdOrderByJoinedAtAsc(id)
                : memberRepository.findByGroupIdAndStatusOrderByJoinedAtAsc(id, status);

        Set<Long> userIds = members.stream().map(PlatGroupMember::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));

        return members.stream().map(m -> {
            User u = userMap.get(m.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("userId", m.getUserId());
            item.put("role", m.getRole());
            item.put("status", m.getStatus());
            item.put("joinedAt", m.getJoinedAt());
            item.put("applyReason", m.getApplyReason());
            item.put("requestedAt", m.getCreatedAt());
            item.put("username", u != null ? u.getUsername() : "");
            item.put("avatarUrl", u != null ? u.getProfilePhoto() : "");
            return item;
        }).collect(Collectors.toList());
    }

    @PostMapping("/{id}/members/{memberId}/approve")
    @Transactional
    public Map<String, Object> approveMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestHeader("Authorization") String authHeader) {

        requireManagerRole(id, authHeader);

        PlatGroupMember target = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member record not found"));
        if (!"pending".equals(target.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request status cannot be changed");
        }

        target.setStatus("approved");
        target.setJoinedAt(LocalDateTime.now());
        target.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(target);

        PlatGroup group = groupRepository.findById(id).orElseThrow();
        group.setMemberCount((group.getMemberCount() == null ? 0 : group.getMemberCount()) + 1);
        groupRepository.save(group);

        return Map.of("approved", true, "message", "Request approved");
    }

    @PostMapping("/{id}/members/{memberId}/reject")
    @Transactional
    public Map<String, Object> rejectMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestHeader("Authorization") String authHeader) {

        requireManagerRole(id, authHeader);

        PlatGroupMember target = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member record not found"));
        if (!"pending".equals(target.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request status cannot be changed");
        }

        target.setStatus("rejected");
        target.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(target);

        return Map.of("rejected", true, "message", "Request rejected");
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?Posts 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾?

    @GetMapping("/{id}/posts")
    public List<Map<String, Object>> getPosts(@PathVariable Long id) {
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        List<PlatGroupPost> posts = postRepository.findByGroupIdAndStatusOrderByCreatedAtDesc(id, "published");
        Set<Long> userIds = posts.stream().map(PlatGroupPost::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));

        return posts.stream().map(p -> {
            User u = userMap.get(p.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("content", p.getContent());
            item.put("imageUrls", p.getImageUrls());
            item.put("type", "post");
            item.put("likeCount", 0);
            item.put("createdAt", p.getCreatedAt());
            item.put("authorName", u != null ? u.getUsername() : "");
            item.put("authorAvatar", u != null ? u.getProfilePhoto() : "");
            return item;
        }).collect(Collectors.toList());
    }

    @PostMapping("/{id}/posts")
    @Transactional
    public Map<String, Object> createPost(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {

        User user = adminAuthService.requireUser(authHeader);
        memberRepository.findByGroupIdAndUserId(id, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Join the group before posting"));

        PlatGroupPost post = new PlatGroupPost();
        post.setGroupId(id);
        post.setUserId(user.getUserid());
        post.setContent(String.valueOf(payload.getOrDefault("content", "")));
        post.setImageUrls((String) payload.get("imageUrls"));
        post.setStatus("published");
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        return Map.of("id", post.getId(), "message", "Post published successfully");
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?Notices 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻?

    @GetMapping("/{id}/notices")
    public List<Map<String, Object>> getNotices(@PathVariable Long id) {
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        return noticeRepository.findByGroupIdAndStatusOrderByCreatedAtDesc(id, "published").stream()
                .map(n -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", n.getId());
                    item.put("title", n.getTitle());
                    item.put("content", n.getContent());
                    item.put("createdAt", n.getCreatedAt());
                    return item;
                }).collect(Collectors.toList());
    }

    @PostMapping("/{id}/notices")
    @Transactional
    public Map<String, Object> createNotice(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {

        User user = adminAuthService.requireUser(authHeader);
        requireManagerRole(id, authHeader);

        PlatGroupNotice notice = new PlatGroupNotice();
        notice.setGroupId(id);
        notice.setTitle(String.valueOf(payload.getOrDefault("title", "")));
        notice.setContent(String.valueOf(payload.getOrDefault("content", "")));
        notice.setCreatedBy(user.getUserid());
        notice.setStatus("published");
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        noticeRepository.save(notice);

        return Map.of("id", notice.getId(), "message", "Notice published successfully");
    }

    // 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛?Helpers 闂傚倸鍊风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻娑氣偓锝庡亝瀹曞矂鏌＄仦鐣屝х€规洘顨嗗鍕節娴ｅ壊妫滈梻鍌氬€风粈渚€宕崸妤€鍌ㄦ繝濠傜墕绾惧鏌熼崜褏甯涢柣鎾冲暣閺屾稖绠涢幙鍐┬︽繛瀛樼矒缁犳牕顫忓ú顏勭闁圭粯甯掓潏鍛存⒑缁嬫鍎愰柟鐟版喘瀵顓兼径濠勵槯婵犮垼娉涢敃锝嗙珶閺囥垺鈷掑ù锝囶焾閺嗛亶鏌涘Ο鑽ょ煉鐎规洘鍨块獮妯肩磼濡厧甯楅梻浣侯焾缁绘劙藝椤栨稓顩插Δ锝呭暞閳锋垿鏌涢幇顓炵祷閻㈩垬鍔戦弻?

    private Map<String, Object> buildGroupSummary(PlatGroup g, Map<Long, PlatGroupMember> memberMap) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", g.getId());
        item.put("slug", g.getSlug());
        item.put("name", g.getName());
        item.put("coverUrl", g.getCoverUrl());
        item.put("type", g.getType());
        String typeName = TYPE_LABELS.getOrDefault(g.getType(), g.getType());
        item.put("typeName", typeName);
        item.put("category", typeName);
        item.put("region", g.getRegion());
        item.put("location", g.getRegion());
        item.put("memberCount", g.getMemberCount() == null ? 0 : g.getMemberCount());
        item.put("description", g.getDescription());
        item.put("joinMode", g.getJoinMode());
        item.put("joinType", "free".equals(g.getJoinMode()) ? "open" : "approval");
        item.put("status", g.getStatus());
        item.put("createdAt", g.getCreatedAt());

        PlatGroupMember m = memberMap.get(g.getId());
        if (m != null) {
            boolean isApproved = "approved".equals(m.getStatus());
            boolean isPending = "pending".equals(m.getStatus());
            boolean isManager = isApproved && ("owner".equals(m.getRole()) || "admin".equals(m.getRole()));
            item.put("isMember", isApproved);
            item.put("managed", isManager);
            item.put("hasPendingRequest", isPending);
            item.put("myStatus", isManager ? "managed" : isApproved ? "joined" : isPending ? "pending" : "none");
        } else {
            item.put("isMember", false);
            item.put("managed", false);
            item.put("hasPendingRequest", false);
            item.put("myStatus", "none");
        }
        return item;
    }

    private Map<Long, PlatGroupMember> loadUserMemberMap(Long userId) {
        if (userId == null) return Collections.emptyMap();
        return memberRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(PlatGroupMember::getGroupId, m -> m, (a, b) -> a));
    }

    private PlatGroup resolveGroup(String idOrSlug) {
        try {
            long id = Long.parseLong(idOrSlug);
            return groupRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        } catch (NumberFormatException e) {
            return groupRepository.findBySlug(idOrSlug)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        }
    }

    private String readString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String strOf(Map<String, Object> m, String key) {
        return m.containsKey(key) && m.get(key) != null ? String.valueOf(m.get(key)) : null;
    }

    private Long resolveOptionalUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            return adminAuthService.requireUser(authHeader).getUserid();
        } catch (Exception e) {
            return null;
        }
    }

    private void requireManagerRole(Long groupId, String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        memberRepository.findByGroupIdAndUserId(groupId, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus())
                        && ("owner".equals(m.getRole()) || "admin".equals(m.getRole())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission"));
    }

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private String formatRelativeTime(LocalDateTime dt) {
        if (dt == null) return "";
        long minutes = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " minutes ago";
        if (minutes < 1440) return (minutes / 60) + " hours ago";
        return dt.format(DF);
    }
}
