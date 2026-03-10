package com.newslens_backend.domain.news;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 뉴스 엔티티
 *
 * 국내/해외 뉴스를 저장하는 핵심 테이블
 * - 국내: title만 저장, rank_score로 정렬
 * - 해외: title + summary 저장, source_rank로 정렬
 * - 클러스터링: 같은 이슈의 기사들을 묶어서 관리
 */
@Entity  // JPA 엔티티임을 선언
@Table(
        name = "news",  // 실제 테이블명
        indexes = {
                // 성능 최적화를 위한 인덱스들
                @Index(name = "idx_type_created", columnList = "type, created_at"),  // 타입별 최신순 조회
                @Index(name = "idx_cluster", columnList = "cluster_id, is_representative"),  // 클러스터 조회
                @Index(name = "idx_category", columnList = "category"),  // 카테고리별 조회
                @Index(name = "idx_representative", columnList = "type, is_representative, created_at")  // 대표 기사 조회
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_url", columnNames = "url")  // URL 중복 방지
        }
)
@Getter  // Lombok: 모든 필드에 대한 getter 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // Lombok: 기본 생성자 (JPA용, 외부에서 직접 생성 불가)
public class News {

        // ──────────────────────────────────────────────
        //  기본 필드
        // ──────────────────────────────────────────────

        @Id  // 기본키
        @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT
        private Long id;

        /**
         * 뉴스 타입 (DOMESTIC: 국내, GLOBAL: 해외)
         */
        @Enumerated(EnumType.STRING)  // Enum을 문자열로 저장 (DOMESTIC, GLOBAL)
        @Column(nullable = false, length = 20)
        private NewsType type;

        /**
         * 뉴스 제목 (원문 그대로, 가공하지 않음)
         *
         * 국내: 언론사 제목 그대로
         * 해외: RSS 제목 그대로
         */
        @Column(nullable = false, length = 500)
        private String title;

        /**
         * 뉴스 요약 (해외 뉴스만 사용)
         *
         * 국내: null (저장 안 함)
         * 해외: RSS description 또는 LLM 생성 요약
         */
        @Column(columnDefinition = "TEXT")  // TEXT 타입 (긴 문자열)
        private String summary;

        /**
         * 원문 링크 (중복 불가)
         *
         * 크롤러에서 URL 기준으로 중복 체크
         */
        @Column(nullable = false, length = 1000, unique = true)
        private String url;

        /**
         * 언론사명
         *
         * 국내: "조선일보", "중앙일보" 등
         * 해외: "BBC", "CNN" 등
         */
        @Column(nullable = false)
        private String source;

        /**
         * 카테고리
         *
         * POLITICS, ECONOMY, SOCIETY, CULTURE, INTERNATIONAL,
         * SPORTS, ENTERTAINMENT, IT, LIFE, OPINION
         */
        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private NewsCategory category;

        /**
         * 뉴스 발행 시간 (언론사 제공)
         */
        @Column(name = "published_at")
        private LocalDateTime publishedAt;

        // ──────────────────────────────────────────────
        //  정렬용 필드
        // ──────────────────────────────────────────────

        /**
         * 급상승 점수 (국내 전용)
         *
         * 계산식: max(100 - hours_since_published, 76)
         * - 발행 직후: 100점
         * - 24시간 경과: 76점 (최소값)
         *
         * 해외: null
         */
        @Column(name = "rank_score")
        private Double rankScore;

        /**
         * 언론사 제공 순위 (해외 전용)
         *
         * RSS 피드 내 순서 (1 = 최상단 = 가장 중요)
         * - enumerate(feed.entries, start=1)로 할당
         *
         * 국내: null
         */
        @Column(name = "source_rank")
        private Integer sourceRank;

        // ──────────────────────────────────────────────
        //  클러스터링 필드
        // ──────────────────────────────────────────────

        /**
         * 클러스터 ID (UUID)
         *
         * 같은 이슈의 기사들은 동일한 cluster_id를 가짐
         * 예: "대통령 경제 정책 발표" 관련 기사 5개 → 같은 cluster_id
         */
        @Column(name = "cluster_id", length = 36)
        private String clusterId;

        /**
         * 대표 기사 여부
         *
         * true: 클러스터의 대표 기사 (리스트에 노출)
         * false: 관련 기사 (상세 화면에만 노출)
         *
         * 대표 선정 기준:
         * - 국내: rank_score 최댓값
         * - 해외: source_rank 최솟값
         */
        @Column(name = "is_representative", nullable = false)
        private Boolean isRepresentative = false;

        /**
         * 클러스터 크기 (관련 기사 수)
         *
         * 비정규화: 성능을 위해 저장 (N+1 쿼리 방지)
         * 크롤러에서 새 기사 편입 시 UPDATE로 +1 갱신
         *
         * UI 표시: "관련 5건"
         */
        @Column(name = "cluster_size", nullable = false)
        private Integer clusterSize = 1;

        // ──────────────────────────────────────────────
        //  메타 정보
        // ──────────────────────────────────────────────

        /**
         * 생성 시간 (크롤러 수집 시간)
         *
         * TTL 24시간 기준:
         * - created_at < NOW() - INTERVAL 24 HOUR → 삭제
         */
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        /**
         * JPA 콜백: INSERT 전 자동 실행
         *
         * created_at을 현재 시간으로 설정
         */
        @PrePersist
        protected void onCreate() {
                createdAt = LocalDateTime.now();
        }
}