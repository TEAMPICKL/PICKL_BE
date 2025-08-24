package com.likelion.picklbe.domain.dailypricechange.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.likelion.picklbe.domain.dailypricechange.response.ItemDailyPriceChangeResponse;
import com.likelion.picklbe.global.api.kamis.dto.KamisPriceResponse.Item;

@Component
public class DailyPriceChangeMapper {

  public ItemDailyPriceChangeResponse toItemResponse(Item item) {
    double latest = parseFirst(item.getLatestPrice());
    double prev = parseFirst(item.getOneDayAgoPrice());
    double diff = latest - prev;
    double rate = prev == 0 ? 0 : (diff / prev) * 100;

    return ItemDailyPriceChangeResponse.builder()
        .productName(item.getProductName())
        .unit(item.getUnit())
        .latestPrice(round(latest))
        .oneDayAgoPrice(round(prev))
        .priceDiff(round(diff))
        .priceDiffRate(round(rate))
        .build();
  }

  /** Item 전체에서 latestPrice 를 파싱해서 반환 */
  public double parseLatestPrice(Item item) {
    return parseFirst(item.getLatestPrice());
  }

  /** Item 전체에서 oneDayAgoPrice 를 파싱해서 반환 */
  public double parseOneDayAgoPrice(Item item) {
    return parseFirst(item.getOneDayAgoPrice());
  }

  /** List<String> 의 첫 번째 요소를 꺼내서 parse() 로 넘깁니다. 비어있으면 0 반환 */
  private double parseFirst(List<String> prices) {
    if (prices == null || prices.isEmpty()) {
      return 0;
    }
    String first = prices.get(0);
    if (first == null || first.isBlank()) {
      return 0;
    }
    try {
      return Double.parseDouble(first.replace(",", ""));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /** 평균 계산: public 으로 변경 */
  public double avg(List<Double> list) {
    return list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
  }

  /** 소수점 둘째 자리에서 반올림 */
  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}
