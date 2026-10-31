package com.example.ticktracker.exception;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidSightingException.class)
    public ResponseEntity<ApiError> invalidSighting(InvalidSightingException e, HttpServletRequest req) {
        return build(422, "Please fix the highlighted fields", e.getFieldErrors(), req);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> badRequest(BadRequestException e, HttpServletRequest req) {
        return build(400, e.getMessage(), null, req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> badParameter(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return build(400, "Invalid value for parameter '" + e.getName() + "'", null, req);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> tooLarge(MaxUploadSizeExceededException e, HttpServletRequest req) {
        return build(413, "Upload too large. Images must be 5 MB or smaller", null, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> fallback(Exception e, HttpServletRequest req) {
        if (e instanceof ErrorResponse er) { // Spring's own errors: 404, 405, missing parameter...
            return build(er.getStatusCode().value(), e.getMessage(), null, req);
        }
        log.error("Unhandled error on {}", req.getRequestURI(), e);
        return build(500, "Unexpected server error", null, req);
    }

    private ResponseEntity<ApiError> build(int status, String message, Map<String, String> fieldErrors,
            HttpServletRequest req) {
        HttpStatus hs = HttpStatus.resolve(status);
        String reason = hs != null ? hs.getReasonPhrase() : "Error";
        ApiError body = new ApiError(Instant.now().toString(), status, reason, message,
                req.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}