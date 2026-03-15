package com.newslens_backend.domain.news;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 뉴스 엔터티 : 국내/해외 뉴스를 저장하는 테이블
 * - 국내(title 저장, trending_rank로 정렬)
 * - 해외(title + summary 저장, publisher_rank로 정렬)
 * - 클러스터링: 같은 이슈의 기사들을 묶어서 관리
 */
@Entity
@Table(
        name = "news",
        indexes = {
                @Index(name = "idx_type_created", columnList = "type, created_at"),
                @Index(name = "idx_cluster", columnList = "cluster_id, is_representative"),
                @Index(name = "idx_category", columnList = "category"),
                @Index(name = "idx_representative", columnList = "type, is_representative, created_at")
        },
        uniqueConstraints = {
        @UniqueConstraint(name = "uq_url", columnNames = "url")
}
)

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NewsType type;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, length = 1000, unique = true)
    private String url;

    @Column(nullable = false)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NewsCategory category;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // (국내 전용) 급상승 점수
    // 계산식: max(100 - hours_since_published, 76) -> 최소 76
    // 높을수록 상위 노출
    @Column(name = "trending_rank")
    private Double trendingRank;

    // (해외 전용) 언론사 제공 순위
    // enumerate(feed.entries, start=1)로 할당 -> 최상단 1
    // 낮을수록 상위 노출
    @Column(name = "publisher_rank")
    private Integer publisherRank;

    @Column(name = "cluster_id", length = 36)
    private String clusterId;

    @Column(name = "is_representative", nullable = false)
    private Boolean isRepresentative = false;

    @Column(name = "cluster_size", nullable = false)
    private Integer clusterSize = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
