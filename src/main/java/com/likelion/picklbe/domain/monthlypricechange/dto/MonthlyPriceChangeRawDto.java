package com.likelion.picklbe.domain.monthlypricechange.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyPriceChangeRawDto {

  private String code;
  private String message;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Row {

    private String yyyymm; // "202501"
    private String price; // 평균가 (문자열, 콤마 포함 가능)
    private String max; // 최고가
    private String min; // 최저가
  }

  private List<Row> rows;
}
