package com.likelion.picklbe.global.exception;

import com.likelion.picklbe.global.exception.model.BaseErrorCode;

public class CustomException extends RuntimeException {

  private final BaseErrorCode errorCode;

  public CustomException(BaseErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public BaseErrorCode getErrorCode() {
    return this.errorCode;
  }
}
