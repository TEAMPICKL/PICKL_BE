package com.likelion.picklbe.global.exception;

import org.springframework.http.HttpStatus;

import com.likelion.picklbe.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseErrorCode {
  QUIZ_NOT_READY("QUIZ_NOT_READY", "오늘의 퀴즈가 아직 준비되지 않았습니다.", HttpStatus.SERVICE_UNAVAILABLE),
  QUIZ_POOL_EMPTY("QUIZ_POOL_EMPTY", "출제 가능한 퀴즈가 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ALREADY_ATTEMPTED("ALREADY_ATTEMPTED", "오늘은 이미 참여했습니다.", HttpStatus.CONFLICT),
  BAD_REQUEST("BAD_REQUEST", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
  UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
