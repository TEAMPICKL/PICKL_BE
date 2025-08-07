package com.likelion.picklbe.domain.averageprice.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CategoryAveragePriceResponse {
  @Schema(description = "분류(소매/도매)", example = "소매")
  private String productClsName;

  @Schema(description = "카테고리 코드", example = "400")
  private String categoryCode;

  @Schema(description = "카테고리 이름", example = "채소류")
  private String categoryName;

  @Schema(description = "카테고리에 해당하는 품목들의 평균 최신 가격", example = "4120.5")
  private double avgLatestPrice;

  @Schema(description = "카테고리에 해당하는 품목들의 평균 하루 전 가격", example = "3900.0")
  private double avgOneDayAgoPrice;

  @Schema(description = "평균 가격 차이 (최신가 평균 - 전일가 평균)", example = "220.5")
  private double priceDiff;

  @Schema(description = "평균 가격 변동률 (%)", example = "5.65")
  private double priceDiffRate;
}
