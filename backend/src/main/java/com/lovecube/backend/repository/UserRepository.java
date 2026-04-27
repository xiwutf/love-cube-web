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

    List<User> findByGenderAndUseridNot(Integer gender, Long userId);

    List<User> findByUseridNot(Long userId);

    User findByPhoneNumber(String phoneNumber);

    User findByEmail(String email);

    User findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    List<User> findByInvitedByUserIdOrderByCreatedAtDesc(Long invitedByUserId);

    long countByCreatedAtGreaterThanEqual(LocalDateTime createdAt);

    long countByUserStatusIgnoreCase(String userStatus);

    @Query("SELECT COUNT(u) FROM User u WHERE (u.phoneNumber IS NULL OR u.phoneNumber <> :hiddenPhone)")
    long countVisibleUsers(@Param("hiddenPhone") String hiddenPhone);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :createdAt AND (u.phoneNumber IS NULL OR u.phoneNumber <> :hiddenPhone)")
    long countVisibleUsersCreatedSince(@Param("createdAt") LocalDateTime createdAt, @Param("hiddenPhone") String hiddenPhone);

    @Query("SELECT COUNT(u) FROM User u WHERE LOWER(COALESCE(u.userStatus, '')) = LOWER(:userStatus) AND (u.phoneNumber IS NULL OR u.phoneNumber <> :hiddenPhone)")
    long countVisibleUsersByStatus(@Param("userStatus") String userStatus, @Param("hiddenPhone") String hiddenPhone);
}
