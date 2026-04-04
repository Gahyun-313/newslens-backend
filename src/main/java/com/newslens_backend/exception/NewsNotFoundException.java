package com.newslens_backend.exception;

/**
 * 뉴스를 찾을 수 없을 때 발생하는 예외
 */
public class NewsNotFoundException extends RuntimeException {

    public NewsNotFoundException(String message) {
        super(message);
    }

    public NewsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
