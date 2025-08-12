package com.likelion.picklbe.global.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.likelion.picklbe.global.exception.model.BaseErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // CustomException, ApiException 처리
  @ExceptionHandler({CustomException.class, ApiException.class})
  public ResponseEntity<?> handleDomainExceptions(RuntimeException ex) {
    // 두 예외 모두 BaseErrorCode를 갖는다고 가정하고 안전하게 분기
    BaseErrorCode errorCode =
        (ex instanceof CustomException)
            ? ((CustomException) ex).getErrorCode()
            : ((ApiException) ex).getErrorCode();

    log.error("Custom/API 예외 발생: {} - {}", errorCode.getCode(), errorCode.getMessage(), ex);

    var body =
        Map.of(
            "success",
            false,
            "code",
            errorCode.getCode(),
            "message",
            errorCode.getMessage(),
            "data",
            null);
    return ResponseEntity.status(errorCode.getStatus()).body(body);
  }

  // Validation 실패 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
    String errorMessages =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
            .collect(Collectors.joining(" | "));
    log.warn("Validation 오류 발생: {}", errorMessages);

    var body =
        Map.of(
            "success", false, "code", "VALIDATION_ERROR", "message", errorMessages, "data", null);
    return ResponseEntity.badRequest().body(body);
  }

  // 잘못된 HTTP Method 사용 처리
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
    log.warn("지원하지 않는 HTTP Method: {}", ex.getMessage());
    var body =
        Map.of(
            "success",
            false,
            "code",
            "METHOD_NOT_ALLOWED",
            "message",
            "지원하지 않는 HTTP Method입니다.",
            "data",
            null);
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
  }

  // 예상하지 못한 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleException(Exception ex) {
    log.error("Server 오류 발생: {}", ex.getMessage(), ex);

    // VWORLD 요청/응답 에러 확인용 임시 로그
    if (ex.getCause() != null) {
      log.error("Cause: {}", ex.getCause().getMessage());
    }

    // 임시로 원인 메시지 같이 반환 (보안상 운영환경에선 상세 메시지 노출 지양 권장)
    var body =
        Map.of(
            "success",
            false,
            "code",
            GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            "message",
            "예상하지 못한 서버 오류가 발생했습니다. 원인: " + ex.getMessage(),
            "data",
            null);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
