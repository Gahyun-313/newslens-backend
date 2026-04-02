package com.newslens_backend.controller;

import com.newslens_backend.domain.news.News;
import com.newslens_backend.domain.news.NewsCategory;
import com.newslens_backend.dto.ClusterDetailResponse;
import com.newslens_backend.dto.DomesticNewsDto;
import com.newslens_backend.dto.GlobalNewsDto;
import com.newslens_backend.dto.NewsListResponse;
import com.newslens_backend.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 뉴스 API Controller
 * - 국내/해외 뉴스 조회
 * - 클러스터 상세 조회
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    // ──────────────────────────────────────────────
    //  국내 뉴스
    // ──────────────────────────────────────────────
    /**
     * 국내 뉴스 조회
     * - GET /api/news/domestic (전체)
     * - GET /api/news/domestic?category=POLITICS (단일 카테고리)
     * - GET /api/news/domestic?categories=POLITICS,IT (복수 카테고리)
     *
     * 정렬: trending_rank DESC (높을수록 상위)
     *
     * @param category 단일 카테고리 필터 (선택)
     * @param categories 복수 카테고리 필터 (쉼표 구분, 선택)
     * @return 뉴스 리스트 (최대 10개)
     */
    @GetMapping("/domestic")
    public ResponseEntity<NewsListResponse<DomesticNewsDto>> getDomestic(
            @RequestParam(required = false) NewsCategory category,
            @RequestParam(required = false) String categories
            ) {
        Page<News> result;

        // 복수 카테고리 조회
        if (categories != null && categories.isBlank()) {
            // 쉼표로 분리 -> Enum 변환
            List<NewsCategory> categoryList = Arrays.stream(categories.split(","))
                    .map(String::trim) // 공백 제거
                    .map(NewsCategory::valueOf) // String -> Enum
                    .toList();
            result = newsService.getDomesticCategories(categoryList);

        // 단일 카테고리 조회
        } else if (category != null) {
            result = newsService.getDomesticByCategory(category);

        // 전체 조회
        } else {
            result = newsService.getDomesticAll();
        }

        // Entity -> DTO 변환
        List<DomesticNewsDto> dtoList = result.getContent()
                .stream()
                .map(DomesticNewsDto::from) // News -> DomesticNewsDto
                .collect(Collectors.toList());

        // 응답 래퍼로 감싸서 반환
        return ResponseEntity.ok(NewsListResponse.of(dtoList));
    }

    // ──────────────────────────────────────────────
    //  해외 뉴스
    // ──────────────────────────────────────────────
    /**
     * 해외 뉴스 조회 (언론사별)
     * - GET /api/news/global (기본: BBC)
     * - GET /api/news/global?source=CNN
     *
     * 정렬: publisher_rank ASC (낮을수록 상위)
     *
     * @param source 언론사명
     * @return 해외 뉴스 리스트 (최대 10개)
     */
    @GetMapping("/global")
    public ResponseEntity<NewsListResponse<GlobalNewsDto>> getGlobal(
            @RequestParam(defaultValue = "BBC") String source
    ) {
        // 언론사별 조회
        Page<News> result = newsService.getGlobalBySource(source);

        // Entity -> DTO 변환
        List<GlobalNewsDto> dtoList = result.getContent()
                .stream()
                .map(GlobalNewsDto::from) // News -> GlobalNewsDto
                .collect(Collectors.toList());

        return ResponseEntity.ok(NewsListResponse.of(dtoList));
    }

    /**
     * 해외 언론사 목록 조회
     * - GET /api/news/global/sources
     *
     * @return 언론사 목록
     */
    @GetMapping("/global/sources")
    public ResponseEntity<List<String>> getGlobalSources() {
        return ResponseEntity.ok(newsService.getGlobalSources());
    }

    // ──────────────────────────────────────────────
    //  클러스터
    // ──────────────────────────────────────────────
    /**
     * 클러스터 상세 조회
     * - GET /api/news/cluster/{clusterId}
     * 같은 이슈의 관련 기사 전체 반환
     *
     * @param clusterId 클러스터ID (UUID)
     * @return 클러스터 상세 (대표기사 + 관련기사 목록)
     */
    public ResponseEntity<ClusterDetailResponse> getClusterDetail(
            @PathVariable String clusterId
    ) {
        // 클러스터 ID로 관련 기사 전체 조회
        List<News> articles = newsService.getClusterDetail(clusterId);

        // 클러스터가 비어있으면 404 반환
        if (articles.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 대표 기사 찾기 (is_representative = true)
        // 없으면 첫 번째 기사를 대표로
        News representative = articles.stream()
                .filter(News::getIsRepresentative)
                .findFirst()
                .orElse(articles.get(0));

        // 관련 기사 목록 생성 (간단한 정보만)
        List<ClusterDetailResponse.RelatedArticle> relatedArticles = articles.stream()
                .map(news -> ClusterDetailResponse.RelatedArticle.builder()
                        .id(news.getId())
                        .title(news.getTitle())
                        .url(news.getUrl())
                        .source(news.getSource())
                        .isRepresentative(news.getIsRepresentative())
                        .build())
                .collect(Collectors.toList());

        // 응답 DTO 생성
        ClusterDetailResponse response = ClusterDetailResponse.builder()
                .clusterId(clusterId)
                .representative(representative) // 대표기사 (전체 정보)
                .relatedArticles(relatedArticles) // 관련기사 (간단 정보)
                .totalCount(articles.size()) // 총 개수
                .build();

        return ResponseEntity.ok(response);
    }

}
