package com.newslens_backend.repository;

import com.newslens_backend.domain.news.News;
import com.newslens_backend.domain.news.NewsCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 뉴스 Repository
 * - 국내/해외 뉴스 조회, 카테고리/언론사 필터링, 클러스터 조회
 */

public interface NewsRepository extends JpaRepository<News, Long> {
    // ──────────────────────────────────────────────
    // 국내 뉴스 조회
    // ──────────────────────────────────────────────

    // 국내 전체 (trending_rank 내림차순)
    @Query("SELECT n FROM News n " +
        "WHERE n.type = 'DOMESTIC' " +
        "AND n.isRepresentative = true " +
        "AND n.createdAt > :cutoff " +
        "ORDER BY n.trendingRank DESC")
    Page<News> findDomesticAll(
            @Param("cuttoff") LocalDateTime cutoff,
            Pageable pageable
    );

    // 국내 단일 카테고리 필터링
    @Query("SELECT n FROM News n " +
        "WHERE n.type = 'DOMESTIC' " +
        "AND n.isRepresentative = true " +
        "AND n.category = :category " +
        "AND n.createdAt > :cutoff " +
        "ORDER BY n.trendingRank DESC")
    Page<News> findDomesticByCategory(
            @Param("category") NewsCategory category,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    // 국내 복수 카테고리 필터링
    @Query("SELECT n FROM News n " +
            "WHERE n.type = 'DOMESTIC' " +
            "AND n.isRepresentative = true " +
            "AND n.category IN :categories " +
            "AND n.createdAt > :cutoff " +
            "ORDER BY n.trendingRank DESC")
    Page<News> findDomesticByCategories(
            @Param("categories") List<NewsCategory> categories,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    // ──────────────────────────────────────────────
    //  해외 뉴스 조회
    // ──────────────────────────────────────────────

    // 해외 언론사별 (publisher_rank 오름차순)
    @Query("SELECT n FROM News n " +
            "WHERE n.type = 'GLOBAL' " +
            "AND n.isRepresentative = true " +
            "AND n.source = :source " +
            "AND n.createdAt > :cutoff " +
            "ORDER BY n.publisherRank ASC")
    Page<News> findGlobalBySource(
            @Param("source") String source,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    // 해외 언론사 목록 (토글 UI용)
    @Query("SELECT DISTINCT n.source FROM News n " +
            "WHERE n.type = 'GLOBAL' " +
            "AND n.createdAt > :cutoff " +
            "ORDER BY n.source ASC")
    List<String> findDistinctGlobalSources(
            @Param("cutoff") LocalDateTime cutoff
    );

    // ──────────────────────────────────────────────
    //  클러스터 상세 조회
    // ──────────────────────────────────────────────

    // 클러스터 상세 (관련 기사 전체)
    @Query("SELECT n FROM News n " +
            "WHERE n.clusterId = :clusterId " +
            "ORDER BY " +
            "CASE WHEN n.type = 'DOMESTIC' THEN n.trendingRank ELSE 0 END DESC, " +
            "CASE WHEN n.type = 'GLOBAL' THEN n.publisherRank ELSE 999999 END ASC")
    List<News> findByClusterId(
            @Param("clusterId") String clusterId
    );
}