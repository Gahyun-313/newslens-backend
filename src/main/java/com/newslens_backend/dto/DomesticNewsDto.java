package com.newslens_backend.dto;

import com.newslens_backend.domain.news.News;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 국내 뉴스 DTO
 */
@Getter
@Builder
public class DomesticNewsDto {
    private Long id;
    private String title;
    private String url;
    private String source;
    private String category;
    private String clusterId;
    private Integer clusterSize;
    private LocalDateTime createdAt;

    /**
     * Entity → DTO 변환
     */
    public static DomesticNewsDto from(News news) {
        return DomesticNewsDto.builder()
                .id(news.getId())
                .title(news.getTitle())
                .url(news.getUrl())
                .source(news.getSource())
                .category(news.getCategory().name())
                .clusterId(news.getClusterId())
                .clusterSize(news.getClusterSize())
                .createdAt(news.getCreatedAt())
                .build();
    }
}
