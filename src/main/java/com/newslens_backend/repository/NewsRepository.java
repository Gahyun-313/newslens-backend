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
 *
 * Spring Data JPA를 사용한 데이터베이스 접근 계층
 * - 국내/해외 뉴스 조회
 * - 카테고리/언론사 필터링
 * - 클러스터링 관련 조회
 */
public interface NewsRepository extends JpaRepository<News, Long> {

    // ──────────────────────────────────────────────
    //  국내 뉴스 조회
    // ──────────────────────────────────────────────

    /**
     * 국내 대표 기사 — 전체 조회
     *
     * @param cutoff 24시간 전 시간 (TTL)
     * @param pageable 페이지 정보 (LIMIT 10)
     * @return 대표 기사 목록 (rank_score 내림차순)
     */
    @Query("SELECT n FROM News n " +
            "WHERE n.type = 'DOMESTIC' " +
            "AND n.isRepresentative = true " +
            "AND n.createdAt > :cutoff " +
            "ORDER BY n.rankScore DESC")
    Page<News> findDomesticAll(
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    /**
     * 국내 대표 기사 — 단일 카테고리 필터
     *
     * @param category 카테고리 (POLITICS, IT 등)
     * @param cutoff 24시간 전 시간
     * @param pageable 페이지 정보
     * @return 해당 카테고리 대표 기사 목록
     */
    @Query("SELECT n FROM News n " +
            "WHERE n.type = 'DOMESTIC' " +
            "AND n.isRepresentative = true " +
            "AND n.category = :category " +
            "AND n.createdAt > :cutoff " +
            "ORDER BY n.rankScore DESC")
    Page<News> findDomesticByCategory(
            @Param("category") NewsCategory category,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    /**
     * 국내 대표 기사 — 복수 카테고리 필터
     *
     * 토글 UI에서 여러 카테고리 선택 시 사용
     * 예: [정치, IT] 선택 → 정치 + IT 뉴스만 표시
     *
     * @param categories 카테고리 목록
     * @param cutoff 24시간 전 시간
     * @param pageable 페이지 정보
     * @return 해당 카테고리들의 대표 기사 목록
     */
    @Query("SELECT n FROM News n " +
            "WHERE n.type = 'DOMESTIC' " +
            "AND n.isRepresentative = true " +
            "AND n.category IN :categories " +
            "AND n.createdAt > :cutoff " +
            "ORDER BY n.rankScore DESC")
    Page<News> findDomesticByCategories(
            @Param("categories") List<NewsCategory> categories,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    // ──────────────────────────────────────────────
    //  해외 뉴스 조회
    // ──────────────────────────────────────────────

    /**
     * 해외 대표 기사 — 언론사별 조회
     *
     * 단일 언론사만 선택 가능 (토글 UI)
     *
     * @param source 언론사명 (BBC, CNN 등)
     * @param cutoff 24시간 전 시간
     * @param pageable 페이지 정보
     * @return 해당 언론사 대표 기사 목록 (source_rank 오름차순)
     */
    @Query("SELECT n FROM News n " +
            "WHERE n.type = 'GLOBAL' " +
            "AND n.isRepresentative = true " +
            "AND n.source = :source " +
            "AND n.createdAt > :cutoff " +
            "ORDER BY n.sourceRank ASC")
    Page<News> findGlobalBySource(
            @Param("source") String source,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    /**
     * 해외 언론사 목록 조회 (DISTINCT)
     *
     * 토글 UI에 표시할 언론사 목록
     * 최근 24시간 이내 수집된 언론사만 반환
     *
     * @param cutoff 24시간 전 시간
     * @return 언론사 목록 (알파벳 순)
     */
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

    /**
     * 클러스터에 속한 모든 기사 조회
     *
     * 뉴스 카드 클릭 시 관련 기사 전체 목록 표시
     *
     * @param clusterId 클러스터 ID (UUID)
     * @return 클러스터 내 모든 기사 (정렬: 국내 rank_score DESC, 해외 source_rank ASC)
     */
    @Query("SELECT n FROM News n " +
            "WHERE n.clusterId = :clusterId " +
            "ORDER BY " +
            "CASE WHEN n.type = 'DOMESTIC' THEN n.rankScore ELSE 0 END DESC, " +
            "CASE WHEN n.type = 'GLOBAL' THEN n.sourceRank ELSE 999999 END ASC")
    List<News> findByClusterId(@Param("clusterId") String clusterId);
}
