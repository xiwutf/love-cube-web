package com.lovecube.backend.repository;

import com.lovecube.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
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
     * 首页「为你推荐」：仅在已开通联谊且允许展示的用户中随机，避免先随机再过滤导致常为 0 条。
     */
    @Query(value = "SELECT * FROM users WHERE (user_status IS NULL OR LOWER(user_status) <> 'disabled') "
            + "AND fellowship_enabled = 1 AND fellowship_match_visible = 1 "
            + "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<User> findRandomVisibleFellowshipUsers(@Param("limit") int limit);

    /**
     * 首页「新人」：同上，按注册时间倒序取可见用户。
     */
    @Query(value = "SELECT * FROM users WHERE (user_status IS NULL OR LOWER(user_status) <> 'disabled') "
            + "AND fellowship_enabled = 1 AND fellowship_match_visible = 1 "
            + "ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<User> findNewcomersVisibleFellowshipUsers(@Param("limit") int limit);

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

    List<User> findByGenderAndUseridNot(Integer gender, Long userId);

    List<User> findByUseridNot(Long userId);

    User findByPhoneNumber(String phoneNumber);

    User findByEmail(String email);

    User findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    List<User> findByInvitedByUserIdOrderByCreatedAtDesc(Long invitedByUserId);

    List<User> findByRoleInOrderByCreatedAtAsc(List<String> roles);

    long countByCreatedAtGreaterThanEqual(LocalDateTime createdAt);

    long countByUserStatusIgnoreCase(String userStatus);

    @Query("SELECT COUNT(u) FROM User u WHERE (u.phoneNumber IS NULL OR u.phoneNumber <> :hiddenPhone)")
    long countVisibleUsers(@Param("hiddenPhone") String hiddenPhone);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :createdAt AND (u.phoneNumber IS NULL OR u.phoneNumber <> :hiddenPhone)")
    long countVisibleUsersCreatedSince(@Param("createdAt") LocalDateTime createdAt, @Param("hiddenPhone") String hiddenPhone);

    @Query("SELECT COUNT(u) FROM User u WHERE LOWER(COALESCE(u.userStatus, '')) = LOWER(:userStatus) AND (u.phoneNumber IS NULL OR u.phoneNumber <> :hiddenPhone)")
    long countVisibleUsersByStatus(@Param("userStatus") String userStatus, @Param("hiddenPhone") String hiddenPhone);
}
