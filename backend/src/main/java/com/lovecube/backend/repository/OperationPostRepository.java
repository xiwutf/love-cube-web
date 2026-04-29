package com.lovecube.backend.repository;

import com.lovecube.backend.entity.OperationPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OperationPostRepository extends JpaRepository<OperationPost, String> {

    @Query("SELECT p FROM OperationPost p WHERE p.status = :status ORDER BY p.isTop DESC, p.publishTime DESC")
    List<OperationPost> findPublished(@Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM OperationPost p WHERE p.status = :status AND p.type = :type ORDER BY p.isTop DESC, p.publishTime DESC")
    List<OperationPost> findByType(@Param("status") String status, @Param("type") String type, Pageable pageable);

    @Query("SELECT p FROM OperationPost p WHERE p.status = :status AND p.scope IN ('all', :scope) ORDER BY p.isTop DESC, p.publishTime DESC")
    List<OperationPost> findByScope(@Param("status") String status, @Param("scope") String scope, Pageable pageable);

    @Query("SELECT p FROM OperationPost p WHERE p.status = :status AND p.type = :type AND p.scope IN ('all', :scope) ORDER BY p.isTop DESC, p.publishTime DESC")
    List<OperationPost> findByTypeAndScope(@Param("status") String status, @Param("type") String type, @Param("scope") String scope, Pageable pageable);

    @Query("SELECT p FROM OperationPost p WHERE p.status = 'published' ORDER BY p.isTop DESC, p.publishTime DESC")
    List<OperationPost> findLatestPublished(Pageable pageable);
}
