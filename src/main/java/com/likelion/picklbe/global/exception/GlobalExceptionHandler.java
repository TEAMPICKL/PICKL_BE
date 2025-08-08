package com.likelion.picklbe.global.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.likelion.picklbe.global.exception.model.BaseErrorCode;
import com.likelion.picklbe.global.response.BaseResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 커스텀 예외 처리
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<BaseResponse<Object>> handleCustomException(CustomException ex) {
    BaseErrorCode errorCode = ex.getErrorCode();
    log.error("Custom 예외 발생: {}", ex.getMessage());
    return ResponseEntity.status(errorCode.getStatus())
        .body(BaseResponse.error(errorCode.getStatus().value(), ex.getMessage()));
  }

  // Validation 실패 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<Object>> handleValidationException(
      MethodArgumentNotValidException ex) {
    String errorMessages =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
            .collect(Collectors.joining(" | "));
    log.warn("Validation 오류 발생: {}", errorMessages);
    return ResponseEntity.badRequest().body(BaseResponse.error(400, errorMessages));
  }

  // 잘못된 HTTP Method 사용 처리
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<BaseResponse<Object>> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    log.warn("지원하지 않는 HTTP Method: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(BaseResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), "지원하지 않는 HTTP Method입니다."));
  }

  // 예상하지 못한 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Object>> handleException(Exception ex) {
    log.error("Server 오류 발생: {}", ex.getMessage(), ex);

    // VWORLD 요청/응답 에러 확인용 임시 로그
    if (ex.getCause() != null) {
      log.error("Cause: {}", ex.getCause().getMessage());
    }

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            BaseResponse.error(
                500, "예상하지 못한 서버 오류가 발생했습니다. 원인: " + ex.getMessage())); // ← 임시로 원인 메시지 같이 반환
  }
}
