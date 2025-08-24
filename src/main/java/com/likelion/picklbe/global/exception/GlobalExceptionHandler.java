package com.likelion.picklbe.global.exception;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.likelion.picklbe.global.exception.model.BaseErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /* ---------- 공통 응답 빌더 (null-safe, 확장 가능) ---------- */
  private ResponseEntity<Map<String, Object>> buildResponse(
      HttpStatus status, String code, String message, Object data, HttpServletRequest req) {

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", false);
    body.put("code", code);
    body.put("message", message);
    body.put("data", data); // null 허용 (Map.of는 null 금지라 LinkedHashMap 사용)
    body.put("timestamp", OffsetDateTime.now().toString());
    if (req != null) {
      body.put("path", req.getRequestURI());
      body.put("method", req.getMethod());
    }
    return ResponseEntity.status(status).body(body);
  }

  /* ---------- CustomException / ApiException ---------- */
  @ExceptionHandler({CustomException.class, ApiException.class})
  public ResponseEntity<?> handleDomainExceptions(RuntimeException ex, HttpServletRequest req) {
    BaseErrorCode errorCode =
        (ex instanceof CustomException)
            ? ((CustomException) ex).getErrorCode()
            : ((ApiException) ex).getErrorCode();

    HttpStatus status =
        (errorCode != null && errorCode.getStatus() != null)
            ? errorCode.getStatus()
            : HttpStatus.INTERNAL_SERVER_ERROR;
    String code =
        (errorCode != null && errorCode.getCode() != null)
            ? errorCode.getCode()
            : "INTERNAL_SERVER_ERROR";
    String msg =
        (errorCode != null && errorCode.getMessage() != null)
            ? errorCode.getMessage()
            : (ex.getMessage() != null ? ex.getMessage() : "알 수 없는 오류가 발생했습니다.");

    log.error(
        "Custom/API 예외 발생: code={}, msg={}, path={}, method={}",
        code,
        msg,
        (req != null ? req.getRequestURI() : "-"),
        (req != null ? req.getMethod() : "-"),
        ex);

    return buildResponse(status, code, msg, null, req);
  }

  /* ---------- Validation 실패 ---------- */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    String errorMessages =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
            .collect(Collectors.joining(" | "));
    log.warn(
        "Validation 오류 발생: {}, path={}, method={}",
        errorMessages,
        (req != null ? req.getRequestURI() : "-"),
        (req != null ? req.getMethod() : "-"));

    return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", errorMessages, null, req);
  }

  /* ---------- 잘못된 HTTP Method ---------- */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<?> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
    log.warn(
        "지원하지 않는 HTTP Method: {}, path={}, method={}",
        ex.getMessage(),
        (req != null ? req.getRequestURI() : "-"),
        (req != null ? req.getMethod() : "-"));

    return buildResponse(
        HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "지원하지 않는 HTTP Method입니다.", null, req);
  }

  /* ---------- 다운스트림(외부 API: WebClient/RestTemplate) 에러 전문 로깅 ---------- */
  @ExceptionHandler({WebClientResponseException.class})
  public ResponseEntity<?> handleWebClientResponse(
      WebClientResponseException ex, HttpServletRequest req) {
    log.error(
        "[Downstream/WebClient] status={}, path={}, method={}, body={}",
        ex.getStatusCode().value(),
        (req != null ? req.getRequestURI() : "-"),
        (req != null ? req.getMethod() : "-"),
        ex.getResponseBodyAsString(),
        ex);

    return buildResponse(
        HttpStatus.BAD_GATEWAY,
        "DOWNSTREAM_ERROR",
        "외부 서비스 호출 중 오류가 발생했습니다.",
        Map.of(
            "status", ex.getStatusCode().value(),
            "reason", ex.getStatusText()),
        req);
  }

  @ExceptionHandler({RestClientResponseException.class})
  public ResponseEntity<?> handleRestClientResponse(
      RestClientResponseException ex, HttpServletRequest req) {
    log.error(
        "[Downstream/RestTemplate] status={}, path={}, method={}, body={}",
        ex.getRawStatusCode(),
        (req != null ? req.getRequestURI() : "-"),
        (req != null ? req.getMethod() : "-"),
        ex.getResponseBodyAsString(),
        ex);

    return buildResponse(
        HttpStatus.BAD_GATEWAY,
        "DOWNSTREAM_ERROR",
        "외부 서비스 호출 중 오류가 발생했습니다.",
        Map.of(
            "status", ex.getRawStatusCode(),
            "reason", ex.getStatusText()),
        req);
  }

  /* ---------- ResponseStatusException 처리 ---------- */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleRSE(ResponseStatusException ex, WebRequest req) {
    Map<String, Object> body =
        Map.of(
            "message",
            ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString(),
            "code",
            ex.getStatusCode().toString(),
            "timestamp",
            OffsetDateTime.now().toString(),
            "success",
            false);
    return ResponseEntity.status(ex.getStatusCode()).body(body);
  }

  /* ---------- 그 외 예상치 못한 예외 (래핑 원인 추적 포함) ---------- */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleException(Exception ex, HttpServletRequest req) {
    // 원인 추적(과도한 체인 방지)
    Throwable root = ex;
    int depth = 0;
    while (root.getCause() != null && depth++ < 10) {
      root = root.getCause();
    }

    // 1) ResponseStatusException 이면 그대로 전달
    if (root instanceof ResponseStatusException rse) {
      log.warn(
          "RSE(unwrap) 발생: status={}, reason={}, path={}, method={}",
          rse.getStatusCode(),
          rse.getReason(),
          (req != null ? req.getRequestURI() : "-"),
          (req != null ? req.getMethod() : "-"),
          ex);

      String msg = (rse.getReason() != null ? rse.getReason() : rse.getStatusCode().toString());
      return buildResponse(
          HttpStatus.valueOf(rse.getStatusCode().value()),
          rse.getStatusCode().toString(),
          msg,
          null,
          req);
    }

    // 2) 래핑된 WebClient/RestTemplate 예외면 상태코드 복원
    if (root
        instanceof
        org.springframework.web.reactive.function.client.WebClientResponseException
        wcre) {
      log.error(
          "[Downstream/WebClient - wrapped] status={}, body={}, path={}, method={}",
          wcre.getStatusCode().value(),
          wcre.getResponseBodyAsString(),
          (req != null ? req.getRequestURI() : "-"),
          (req != null ? req.getMethod() : "-"),
          ex);

      HttpStatus sc = HttpStatus.valueOf(wcre.getStatusCode().value());
      // Retry-After 헤더를 data로 내려주면 프론트에서 429 쿨다운 파싱에 활용 가능
      String retryAfter = wcre.getHeaders().getFirst("Retry-After");
      Map<String, Object> data =
          (retryAfter != null)
              ? Map.of(
                  "status", sc.value(), "reason", wcre.getStatusText(), "retryAfter", retryAfter)
              : Map.of("status", sc.value(), "reason", wcre.getStatusText());

      return buildResponse(sc, "DOWNSTREAM_ERROR", wcre.getMessage(), data, req);
    }
    if (root instanceof org.springframework.web.client.RestClientResponseException rcre) {
      log.error(
          "[Downstream/RestTemplate - wrapped] status={}, body={}, path={}, method={}",
          rcre.getRawStatusCode(),
          rcre.getResponseBodyAsString(),
          (req != null ? req.getRequestURI() : "-"),
          (req != null ? req.getMethod() : "-"),
          ex);

      HttpStatus sc = HttpStatus.resolve(rcre.getRawStatusCode());
      if (sc == null) {
        sc = HttpStatus.BAD_GATEWAY;
      }
      Map<String, Object> data =
          Map.of("status", rcre.getRawStatusCode(), "reason", rcre.getStatusText());
      return buildResponse(sc, "DOWNSTREAM_ERROR", rcre.getMessage(), data, req);
    }

    // 3) 기타는 서버 내부 오류(원하면 502로 통일 가능)
    log.error(
        "Server 오류 발생(unwrap 포함): {}, path={}, method={}",
        ex.getMessage(),
        (req != null ? req.getRequestURI() : "-"),
        (req != null ? req.getMethod() : "-"),
        ex);

    String msg = "예상하지 못한 서버 오류가 발생했습니다.";
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),
        msg,
        null,
        req);
  }
}
