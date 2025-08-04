package com.likelion.picklbe.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.likelion.picklbe.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
  USERNAME_ALREADY_EXISTS("USER_4001", "이미 존재하는 사용자 아이디입니다.", HttpStatus.BAD_REQUEST),
  PASSWORD_REQUIRED("USER_4002", "비밀번호는 필수입니다.", HttpStatus.BAD_REQUEST),
  USER_NOT_FOUND("USER_4003", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
  INVALID_PASSWORD("USER_4004", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
