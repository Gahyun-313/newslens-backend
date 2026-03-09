package com.newslens_backend.domain.news;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

        @Column(nullable = false, length = 500)
        private String title;

        @Column(columnDefinition = "TEXT")
        private String summary;  // 해외만 사용

        @Column(nullable = false, length = 1000, unique = true)
        private String url;

        @Column(nullable = false)
        private String source;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private NewsCategory category;

        @Column(name = "published_at")
        private LocalDateTime publishedAt;

        @Column(name = "rank_score")
        private Double rankScore;  // 국내 전용

        @Column(name = "source_rank")
        private Integer sourceRank;  // 해외 전용

        @Column(name = "cluster_id", length = 36)
        private String clusterId;  // UUID

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
