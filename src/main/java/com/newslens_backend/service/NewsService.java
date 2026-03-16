package com.newslens_backend.service;

import com.newslens_backend.domain.news.News;
import com.newslens_backend.domain.news.NewsCategory;
import com.newslens_backend.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 뉴스 서비스
 * Repository 호출, TTL/페이징 처리
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {
    private final NewsRepository newsRepository;

    private static final int PAGE_SIZE = 10;    // 고정 10개
    private static final int TTL_HOURS = 24;    // 24시간 TTL

    // ──────────────────────────────────────────────
    //  국내 뉴스
    // ──────────────────────────────────────────────

    // 국내 전체
    public Page<News> getDomesticAll() {
        return newsRepository.findDomesticAll(
                cutoffTime(), pageRequest()
        );
    }

    // 국내 단일 카테고리
    public Page<News> getDomesticByCategory(NewsCategory category) {
        return newsRepository.findDomesticByCategory(
                category, cutoffTime(), pageRequest()
        );
    }

    // 국내 복수 카테고리
    public Page<News> getDomesticCategories(List<NewsCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return getDomesticAll();
        }

        return newsRepository.findDomesticByCategories(
                categories, cutoffTime(), pageRequest()
        );
    }

    // ──────────────────────────────────────────────
    //  해외 뉴스
    // ──────────────────────────────────────────────

    // 해외 언론사별 뉴스
    public Page<News> getGlobalBySource(String source) {
        return newsRepository.findGlobalBySource(
                source, cutoffTime(), pageRequest()
        );
    }

    // 해외 언론사 목록
    public List<String> getGlobalSources() {
        return newsRepository.findDistinctGlobalSources(
                cutoffTime()
        );
    }

    // ──────────────────────────────────────────────
    //  클러스터
    // ──────────────────────────────────────────────

    // 클러스터 상세
    public List<News> getClusterDetail(String clusterId) {
        return newsRepository.findByClusterId(clusterId);
    }

    // ──────────────────────────────────────────────
    //  헬퍼 메서드
    // ──────────────────────────────────────────────

    // TTL 기준 시간 (24시간 전)
    private LocalDateTime cutoffTime() {
        return LocalDateTime.now().minusHours(TTL_HOURS);
    }

    // 페이지 요청 (항상 첫 페이지, 10개)
    private Pageable pageRequest() {
        return PageRequest.of(0, PAGE_SIZE);
    }
}
