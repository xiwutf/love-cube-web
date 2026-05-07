package com.lovecube.backend.repository;

import com.lovecube.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 匹配列表 WHERE（与 {@link #findMatchCandidatesPage} 的 count 子句一致）。
     * 参数约定：genderFilter=-999 表示不按性别过滤；minAgeFilter/maxAgeFilter=-1 表示不按年龄过滤；
     * locationPattern 为 null 不按地区过滤；includeActedInt=1 保留已互动；verifiedOnlyInt=1 仅已认证。
     */
    String MATCH_LIST_WHERE = ""
            + "u.userid <> :currentUserId "
            + "AND (u.user_status IS NULL OR LOWER(u.user_status) <> 'disabled') "
            + "AND u.fellowship_enabled = 1 AND u.fellowship_match_visible = 1 "
            + "AND (:genderFilter < 0 OR u.gender = :genderFilter) "
            + "AND (:minAgeFilter < 0 OR (u.age IS NOT NULL AND u.age >= :minAgeFilter)) "
            + "AND (:maxAgeFilter < 0 OR (u.age IS NOT NULL AND u.age <= :maxAgeFilter)) "
            + "AND (:locationPattern IS NULL OR u.location LIKE :locationPattern) "
            + "AND u.userid NOT IN (SELECT fp.user_id FROM fellowship_profile fp "
            + "WHERE fp.identity_role IN ('guardian_son','guardian_daughter')) "
            + "AND u.userid NOT IN (SELECT b.blocked_user_id FROM user_blacklist b WHERE b.user_id = :currentUserId) "
            + "AND u.userid NOT IN (SELECT b2.user_id FROM user_blacklist b2 WHERE b2.blocked_user_id = :currentUserId) "
            + "AND (:includeActedInt = 1 OR u.userid NOT IN ("
            + "SELECT DISTINCT ui.to_user_id FROM user_interactions ui "
            + "WHERE ui.from_user_id = :currentUserId "
            + "AND ui.interaction_type IN ('LIKE','SUPER_LIKE','SKIP'))) "
            + "AND (:verifiedOnlyInt = 0 OR EXISTS ("
            + "SELECT 1 FROM user_verifications v WHERE v.user_id = u.userid "
            + "AND LOWER(v.status) = 'approved' AND UPPER(v.verify_type) IN ('PHOTO','REALNAME'))) ";

    String MATCH_LIST_ORDER = " ORDER BY (u.profile_photo IS NOT NULL AND TRIM(u.profile_photo) <> '') DESC, "
            + "COALESCE(u.updated_at, u.created_at) DESC, u.userid DESC";
    @Query("SELECT u FROM User u WHERE u.userid != :userId")
    List<User> findAllExceptCurrentUser(@Param("userId") Long userId);

    User findByOpenid(String openid);

    boolean existsByOpenid(String openid);

    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge AND u.gender = :gender AND u.location = :location")
    List<User> findByAgeBetweenAndGenderAndLocation(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("gender") Integer gender,
            @Param("location") String location);

    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge AND u.gender = :gender")
    List<User> findByAgeBetweenAndGender(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("gender") Integer gender);

    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge AND u.location = :location")
    List<User> findByAgeBetweenAndLocation(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("location") String location);

    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge")
    List<User> findByAgeBetween(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge);

    @Query(value = "SELECT * FROM users ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<User> findRandomUsers(@Param("limit") int limit);

    @Query(value = "SELECT * FROM users ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<User> findNewcomers(@Param("limit") int limit);

    /**
     * 首页「为你推荐」候选池：取最近注册的可见用户（走 created_at 索引），由 Service 层随机打散。
     * 避免 ORDER BY RAND() 全表扫描。
     */
    @Query(value = "SELECT * FROM users WHERE (user_status IS NULL OR LOWER(user_status) <> 'disabled') "
            + "AND fellowship_enabled = 1 AND fellowship_match_visible = 1 "
            + "ORDER BY created_at DESC LIMIT :poolSize", nativeQuery = true)
    List<User> findVisibleFellowshipUserPool(@Param("poolSize") int poolSize);

    /**
     * 首页「新人」：按注册时间倒序取可见用户。
     */
    @Query(value = "SELECT * FROM users WHERE (user_status IS NULL OR LOWER(user_status) <> 'disabled') "
            + "AND fellowship_enabled = 1 AND fellowship_match_visible = 1 "
            + "ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<User> findNewcomersVisibleFellowshipUsers(@Param("limit") int limit);

    /**
     * 关键词搜索（用户名/地区/职业），DB 层过滤后分页，避免全量加载再内存过滤。
     */
    @Query(value = "SELECT * FROM users WHERE "
            + "(LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(location) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(occupation) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (user_status IS NULL OR LOWER(user_status) <> 'disabled') "
            + "LIMIT :size OFFSET :offset", nativeQuery = true)
    List<User> searchByKeyword(@Param("keyword") String keyword,
                               @Param("offset") int offset,
                               @Param("size") int size);

    @Query("SELECT u FROM User u " +
            "WHERE u.userid <> :currentUserId " +
            "AND (u.userStatus IS NULL OR LOWER(u.userStatus) <> 'disabled') " +
            "AND u.fellowshipEnabled = true " +
            "AND u.fellowshipMatchVisible = true " +
            "AND (:gender IS NULL OR u.gender = :gender) " +
            "AND (:minAge IS NULL OR (u.age IS NOT NULL AND u.age >= :minAge)) " +
            "AND (:maxAge IS NULL OR (u.age IS NOT NULL AND u.age <= :maxAge)) " +
            "AND (:location IS NULL OR :location = '' OR (u.location IS NOT NULL AND u.location LIKE CONCAT('%', :location, '%')))")
    List<User> findMatchCandidates(
            @Param("currentUserId") Long currentUserId,
            @Param("gender") Integer gender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("location") String location);

    @Query(
            value = "SELECT u.* FROM users u WHERE " + MATCH_LIST_WHERE + MATCH_LIST_ORDER,
            countQuery = "SELECT COUNT(*) FROM users u WHERE " + MATCH_LIST_WHERE,
            nativeQuery = true
    )
    Page<User> findMatchCandidatesPage(
            @Param("currentUserId") Long currentUserId,
            @Param("genderFilter") int genderFilter,
            @Param("minAgeFilter") int minAgeFilter,
            @Param("maxAgeFilter") int maxAgeFilter,
            @Param("locationPattern") String locationPattern,
            @Param("includeActedInt") int includeActedInt,
            @Param("verifiedOnlyInt") int verifiedOnlyInt,
            Pageable pageable);

    List<User> findByGenderAndUseridNot(Integer gender, Long userId);

    List<User> findByUseridNot(Long userId);

    User findByPhoneNumber(String phoneNumber);

    User findByEmail(String email);

    User findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    List<User> findByInvitedByUserIdOrderByCreatedAtDesc(Long invitedByUserId);

    List<User> findByRoleInOrderByCreatedAtAsc(List<String> roles);

    long countByCreatedAtGreaterThanEqual(LocalDateTime createdAt);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    long countByUserStatusIgnoreCase(String userStatus);

    @Query("SELECT COUNT(u) FROM User u WHERE (u.phoneNumber IS NULL OR u.phoneNumber <> :hiddenPhone)")
    long countVisibleUsers(@Param("hiddenPhone") String hiddenPhone);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :createdAt AND (u.phoneNumber IS NULL OR u.phoneNumber <> :hiddenPhone)")
    long countVisibleUsersCreatedSince(@Param("createdAt") LocalDateTime createdAt, @Param("hiddenPhone") String hiddenPhone);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :start AND u.createdAt < :end AND (u.phoneNumber IS NULL OR u.phoneNumber <> :hiddenPhone)")
    long countVisibleUsersCreatedBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("hiddenPhone") String hiddenPhone);

    @Query("SELECT COUNT(u) FROM User u WHERE LOWER(COALESCE(u.userStatus, '')) = LOWER(:userStatus) AND (u.phoneNumber IS NULL OR u.phoneNumber <> :hiddenPhone)")
    long countVisibleUsersByStatus(@Param("userStatus") String userStatus, @Param("hiddenPhone") String hiddenPhone);

    @Query("SELECT u.userid FROM User u WHERE LOWER(COALESCE(u.userStatus, 'normal')) <> 'disabled'")
    Page<Long> findActiveUserIds(Pageable pageable);

    @Query(value = "SELECT * FROM users WHERE (:includeHidden = true OR phone_number IS NULL OR phone_number <> :hiddenPhone) ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<User> findRecentUsersForAdmin(
            @Param("limit") int limit,
            @Param("includeHidden") boolean includeHidden,
            @Param("hiddenPhone") String hiddenPhone);
}
