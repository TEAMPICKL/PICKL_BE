package com.likelion.picklbe.global.api.market.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MarketApiResponseDto {
  private String 시장명;
  private String 소재지도로명주소;
  private String 위도;
  private String 경도;
}
