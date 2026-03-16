package com.newslens_backend.dto;

import lombok.Getter;
import java.util.List;

/**
 * 뉴스 리스트 응답 (국내, 해외 공통)
 */
@Getter
public class NewsListResponse<T> {
    private final List<T> news;

    public NewsListResponse(List<T> news) {
        this.news = news;
    }

    public static <T> NewsListResponse<T> of(List<T> news) {
        return new NewsListResponse<>(news);
    }
}
