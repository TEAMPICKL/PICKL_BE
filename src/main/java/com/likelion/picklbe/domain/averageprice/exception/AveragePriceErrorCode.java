package com.likelion.picklbe.domain.averageprice.exception;

import org.springframework.http.HttpStatus;

import com.likelion.picklbe.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AveragePriceErrorCode implements BaseErrorCode {
  SEASON_ITEM_NOT_FOUND("SEASON_ITEM_404", "존재하지 않는 제철 식재료입니다.", HttpStatus.NOT_FOUND),
  PRICE_DATA_NOT_FOUND("AVERAGE_PRICE_404", "가격 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
  private final String code;
  private final String message;
  private final HttpStatus status;
}
