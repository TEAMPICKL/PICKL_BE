package com.likelion.picklbe.global.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

  private final ErrorCode errorCode;

  private ApiException(ErrorCode ec) {
    super(ec.getMessage());
    this.errorCode = ec;
  }

  public static ApiException of(ErrorCode ec) {
    return new ApiException(ec);
  }
}
