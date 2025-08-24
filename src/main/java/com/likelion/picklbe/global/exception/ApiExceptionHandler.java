package com.likelion.picklbe.global.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
    if ("ALREADY_CLAIMED_TODAY".equals(e.getMessage())) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Map.of("code", "ALREADY_CLAIMED_TODAY", "message", "오늘은 이미 보상을 받았어요."));
    }
    return ResponseEntity.badRequest()
        .body(Map.of("code", "BAD_REQUEST", "message", e.getMessage()));
  }
}
