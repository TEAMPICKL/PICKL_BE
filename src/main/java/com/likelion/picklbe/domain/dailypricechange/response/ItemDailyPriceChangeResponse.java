package com.likelion.picklbe.domain.dailypricechange.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ItemDailyPriceChangeResponse {

  @Schema(description = "상품명", example = "배추")
  private String productName;

  @Schema(description = "단위", example = "1포기")
  private String unit;

  @Schema(description = "최신 가격", example = "3500.0")
  private double latestPrice;

  @Schema(description = "하루 전 가격", example = "3000.0")
  private double oneDayAgoPrice;

  @Schema(description = "가격 차이 (최신가 - 전일가)", example = "500.0")
  private double priceDiff;

  @Schema(description = "가격 변동률 (%)", example = "16.67")
  private double priceDiffRate;

  @Schema(description = "unsplash 사진 url", example = "https://images.unsplash.com/photo-....")
  private String imageUrl;
}
