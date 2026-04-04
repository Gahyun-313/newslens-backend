package com.newslens_backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 * - @RestControllerAdvice: 모든 @RestController에서 발생하는 예외를 한 곳에서 처리
 * - 예외 유형별로 적절한 HTTP 상태 코드와 ErrorResponse를 반환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * NewsNotFoundException 처리 (404 Not Found)
     * - 존재하지 않는 뉴스 ID 조회 시 발생
     */
    @ExceptionHandler(NewsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNewsNotFound(
            NewsNotFoundException ex,
            HttpServletRequest request // 요청 URI를 ErrorResponse에 포함하기 위해 주입
    ) {
        log.error("NewsNotFoundException: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),   // 404
                "Not Found",
                ex.getMessage(),                // ex. "News not found width id: 99"
                request.getRequestURI()         // ex. "/api/news/99"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * IllegalArgumentException 처리 (400 Bad Request)
     * - 잘못된 인자값(음수 페이지 번호, 빈 카테고리 등)이 전달될 때 발생
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.error("IllegalArgumentException: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), // 400
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Validation 실패 처리 (400 Bad Request)
     * - @Valid 어노테이션으로 검증 실패 시 Spring이 자동으로 던지는 예외
     * - 여러 필드 오류를 "필드명: 메시지" 형식으로 합쳐 하나의 문자열로 반환
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage());

        // BindingResult에서 필드 오류 목록을 꺼내 "field: message, field: message" 형태로 조합
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)         // 오류가 여러 개면 콤마로 연결
                .orElse("Validation failed");     // 오류가 없는 경우 fallback

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 그 외 모든 예외 처리 (500 Internal Server Error)
     * - 위 핸들러에서 잡히지 않은 예외의 최종 catch-all
     * - 내부 오류 상세 정보는 클라이언트에게 노출하지 않음 (보안)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        // 스택 트레이스 포함 전체 로깅 (서버 측 디버깅용)
        log.error("Unexpected exception: ", ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),   // 500
                "Internal Server Error",
                "Unexpected error occurred",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
