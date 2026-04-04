package com.newslens_backend.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 에러 응답 DTO
 * - 예외 발생 시 클라이언트에게 반환되는 표준 에러 형식
 * - GlobalExceptionHandler에서 ErrorResponse.of()를 통해 생성
 *
 * 응답 예시:
 * {
 *     "timestamp": "2026-04-03T12:21:00",
 *     "status": 404,
 *     "error": "Not Found",
 *     "message": "News not found with id: 99",
 *     "path": "/api/news/99"
 * }
 */
@Getter
@AllArgsConstructor // 모든 필드를 받는 생성자 자동 생성 (of() 팩토리 메서드에서 사용)
public class ErrorResponse {

    private final LocalDateTime timestamp;  // 예외 발생 시각
    private final int status;               // HTTP 상태 코드
    private final String error;             // HTTP 상태 명칭
    private final String message;           // 예외 상세 메시지 (클라이언트에게 전달)
    private final String path;              // 요청 URI (ex. "/api/news/99")

    /**
     * ErrorResponse 생성 팩토리 메서드
     * - timestamp 호출 시점의 현재 시각으로 자동 설정
     * - new ErrorResponse() 직접 호출 대신 이 메서드를 통해 생성
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path
        );
    }
}
