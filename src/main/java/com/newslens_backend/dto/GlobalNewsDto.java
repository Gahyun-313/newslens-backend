package com.newslens_backend.dto;

import com.newslens_backend.domain.news.News;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 해외 뉴스 DTO
 */
@Getter
@Builder
public class GlobalNewsDto {
    private Long id;
    private String title;
    private String summary;  // 해외 전용
    private String url;
    private String source;
    private String clusterId;
    private Integer clusterSize;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    /**
     * Entity → DTO 변환
     */
    public static GlobalNewsDto from(News news) {
        return GlobalNewsDto.builder()
                .id(news.getId())
                .title(news.getTitle())
                .summary(news.getSummary())
                .url(news.getUrl())
                .source(news.getSource())
                .clusterId(news.getClusterId())
                .clusterSize(news.getClusterSize())
                .publishedAt(news.getPublishedAt())
                .createdAt(news.getCreatedAt())
                .build();
    }
}
