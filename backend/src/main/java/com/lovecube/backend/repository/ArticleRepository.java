package com.lovecube.backend.repository;

import com.lovecube.backend.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, String> {
    List<Article> findByStatusOrderByPublishDateDesc(String status);
}
