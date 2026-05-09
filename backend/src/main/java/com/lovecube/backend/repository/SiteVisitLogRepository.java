package com.lovecube.backend.repository;

import com.lovecube.backend.entity.SiteVisitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SiteVisitLogRepository extends JpaRepository<SiteVisitLog, Long> {
    long countByCreatedAtGreaterThanEqual(LocalDateTime start);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT s.visitorId) FROM SiteVisitLog s WHERE s.createdAt >= :start")
    long countDistinctVisitorIdSince(@Param("start") LocalDateTime start);

    @Query("SELECT COUNT(DISTINCT s.visitorId) FROM SiteVisitLog s WHERE s.createdAt BETWEEN :start AND :end")
    long countDistinctVisitorIdBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT DATE(created_at) AS d, COUNT(*) AS pv, COUNT(DISTINCT visitor_id) AS uv
            FROM site_visit_log
            WHERE created_at BETWEEN :start AND :end
            GROUP BY DATE(created_at)
            ORDER BY d ASC
            """, nativeQuery = true)
    List<Object[]> aggregateTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT path, COUNT(*) AS pv, COUNT(DISTINCT visitor_id) AS uv
            FROM site_visit_log
            WHERE created_at BETWEEN :start AND :end
            GROUP BY path
            ORDER BY pv DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> aggregateTopPages(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("limit") int limit);

    @Query(value = """
            SELECT device_type, COUNT(*) AS cnt
            FROM site_visit_log
            WHERE created_at BETWEEN :start AND :end
            GROUP BY device_type
            ORDER BY cnt DESC
            """, nativeQuery = true)
    List<Object[]> aggregateDevices(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT browser, COUNT(*) AS cnt
            FROM site_visit_log
            WHERE created_at BETWEEN :start AND :end
            GROUP BY browser
            ORDER BY cnt DESC
            """, nativeQuery = true)
    List<Object[]> aggregateBrowsers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT os, COUNT(*) AS cnt
            FROM site_visit_log
            WHERE created_at BETWEEN :start AND :end
            GROUP BY os
            ORDER BY cnt DESC
            """, nativeQuery = true)
    List<Object[]> aggregateOs(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT l.id, l.visitor_id, l.user_id, usr.username, usr.phone_number, l.path, l.ip_address, l.device_type, l.browser, l.created_at
            FROM site_visit_log l
            INNER JOIN (
              SELECT visitor_id, MAX(created_at) AS last_at
              FROM site_visit_log
              GROUP BY visitor_id
            ) latest ON latest.visitor_id = l.visitor_id AND latest.last_at = l.created_at
            LEFT JOIN users usr ON usr.userid = l.user_id
            ORDER BY l.created_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> findLatestVisitors(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = """
            SELECT l.id, l.visitor_id, l.user_id, usr.username, usr.phone_number, l.path, l.ip_address, l.device_type, l.browser, l.created_at
            FROM site_visit_log l
            INNER JOIN (
              SELECT visitor_id, MAX(created_at) AS last_at
              FROM site_visit_log
              GROUP BY visitor_id
            ) latest ON latest.visitor_id = l.visitor_id AND latest.last_at = l.created_at
            INNER JOIN users usr ON usr.userid = l.user_id
            ORDER BY l.created_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> findLatestLoggedInVisitors(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(DISTINCT visitor_id) FROM site_visit_log", nativeQuery = true)
    long countDistinctVisitors();

    @Query(value = """
            SELECT COUNT(*) FROM (
              SELECT visitor_id
              FROM site_visit_log
              WHERE user_id IS NOT NULL
              GROUP BY visitor_id
            ) t
            """, nativeQuery = true)
    long countDistinctLoggedInVisitors();

    @Query(value = """
            SELECT referrer
            FROM site_visit_log
            WHERE created_at BETWEEN :start AND :end
            """, nativeQuery = true)
    List<String> findReferrers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT COUNT(*) FROM (
              SELECT visitor_id FROM site_visit_log WHERE created_at >= :start
              GROUP BY visitor_id HAVING COUNT(*) >= 2
            ) t
            """, nativeQuery = true)
    long countRepeatVisitorsSince(@Param("start") LocalDateTime start);
}
