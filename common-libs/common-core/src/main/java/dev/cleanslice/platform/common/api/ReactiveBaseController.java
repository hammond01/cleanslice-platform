package dev.cleanslice.platform.common.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base reactive controller providing common error handling and response utilities.
 */
@Slf4j
public abstract class ReactiveBaseController {

    /**
     * Wraps data in a successful ApiResponse.
     */
    protected <T> Mono<ResponseEntity<ApiResponse<T>>> successResponse(T data) {
        return Mono.just(ResponseEntity.ok(ApiResponse.success(data)));
    }

    /**
     * Wraps data in a successful ApiResponse with custom message.
     */
    protected <T> Mono<ResponseEntity<ApiResponse<T>>> successResponse(T data, String message) {
        return Mono.just(ResponseEntity.ok(ApiResponse.success(data, message)));
    }

    /**
     * Creates a successful response without data.
     */
    protected <T> Mono<ResponseEntity<ApiResponse<T>>> successResponse(String message) {
        return Mono.just(ResponseEntity.ok(ApiResponse.success(message)));
    }

    /**
     * Creates a not found response.
     */
    protected <T> Mono<ResponseEntity<ApiResponse<T>>> notFoundResponse(String message) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND, message)));
    }

    /**
     * Creates a bad request response.
     */
    protected <T> Mono<ResponseEntity<ApiResponse<T>>> badRequestResponse(String message) {
        return Mono.just(ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST, message)));
    }

    /**
     * Creates a bad request response with validation errors.
     */
    protected <T> Mono<ResponseEntity<ApiResponse<T>>> badRequestResponse(String message, List<String> errors) {
        return Mono.just(ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST, message, errors)));
    }

    /**
     * Creates an internal server error response.
     */
    protected <T> Mono<ResponseEntity<ApiResponse<T>>> internalServerErrorResponse(String message) {
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, message)));
    }

    /**
     * Handles IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public <T> Mono<ResponseEntity<ApiResponse<T>>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return badRequestResponse("Invalid request: " + e.getMessage());
    }

    /**
     * Handles validation exceptions from WebFlux.
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public <T> Mono<ResponseEntity<ApiResponse<T>>> handleValidationException(WebExchangeBindException e) {
        log.warn("Validation error: {}", e.getMessage());
        List<String> errors = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        return badRequestResponse("Validation failed", errors);
    }

    /**
     * Handles generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public <T> Mono<ResponseEntity<ApiResponse<T>>> handleGenericException(Exception e) {
        log.error("Unexpected error", e);
        return internalServerErrorResponse("An unexpected error occurred");
    }
}