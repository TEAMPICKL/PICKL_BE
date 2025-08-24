package com.likelion.picklbe.domain.marketprice.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.likelion.picklbe.domain.marketprice.dto.response.MarketPriceResponse;
import com.likelion.picklbe.domain.marketprice.entity.MarketPrice;
import com.likelion.picklbe.global.api.kamis.dto.KamisPriceResponse.Item;

@Component
public class MarketPriceMapper {

  /** KAMIS Item + 수동가격(있으면 사용, 없으면 0) -> 응답 DTO */
  public MarketPriceResponse toResponse(Item item, Optional<MarketPrice> manualOpt) {
    double market = manualOpt.map(MarketPrice::getMarketPrice).orElse(0.0);
    double mart = manualOpt.map(MarketPrice::getSuperMarketPrice).orElse(0.0);
    String image =
        manualOpt
            .map(MarketPrice::getImageUrl) // ← ✅ 추가
            .filter(s -> !s.isBlank())
            .orElse("");

    return MarketPriceResponse.builder()
        .productName(item.getProductName())
        .unit(item.getUnit())
        .marketPrice(market)
        .superMarketPrice(mart)
        .imageUrl(image)
        .build();
  }

  public MarketPriceResponse fromManual(MarketPrice m) {
    return MarketPriceResponse.builder()
        .productName(m.getProductName())
        .unit(m.getUnit())
        .marketPrice(m.getMarketPrice())
        .superMarketPrice(m.getSuperMarketPrice())
        .imageUrl(m.getImageUrl() == null ? "" : m.getImageUrl())
        .build();
  }
}
