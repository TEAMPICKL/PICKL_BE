package com.likelion.picklbe.domain.marketprice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class MarketPriceResponse {

  @Schema(description = "상품명", example = "배추")
  private String productName;

  @Schema(description = "단위", example = "1포기")
  private String unit; // ✅ 추가

  @Schema(description = "전통시장 가격(원)", example = "27160")
  private double marketPrice;

  @Schema(description = "대형마트 가격(원)", example = "36160")
  private double superMarketPrice;

  @Schema(description = "이미지 URL", example = "https://cdn.example.com/img/cabbage.jpg")
  private String imageUrl;

  @Schema(description = "상품번호(옵션)", example = "A1234")
  private String productNo;
}
