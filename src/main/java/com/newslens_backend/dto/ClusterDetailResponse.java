package com.newslens_backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 클러스터 상세 응답
 */
@Getter
@Builder
public class ClusterDetailResponse {

    private String clusterId;
    private Object representative;  // DomesticNewsDto or GlobalNewsDto
    private List<RelatedArticle> relatedArticles;
    private Integer totalCount;

    @Getter
    @Builder
    public static class RelatedArticle {
        private Long id;
        private String title;
        private String url;
        private String source;
        private Boolean isRepresentative;
    }
}