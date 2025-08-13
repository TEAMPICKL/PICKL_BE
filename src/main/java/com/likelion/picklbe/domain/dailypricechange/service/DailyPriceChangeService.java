package com.likelion.picklbe.domain.dailypricechange.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.likelion.picklbe.domain.dailypricechange.exception.DailyPriceChangeErrorCode;
import com.likelion.picklbe.domain.dailypricechange.mapper.DailyPriceChangeMapper;
import com.likelion.picklbe.domain.dailypricechange.response.CategoryDailyPriceChangeResponse;
import com.likelion.picklbe.domain.dailypricechange.response.ItemDailyPriceChangeResponse;
import com.likelion.picklbe.global.api.kamis.client.KamisPriceClient;
import com.likelion.picklbe.global.api.kamis.dto.KamisPriceResponse.Item;
import com.likelion.picklbe.global.api.unsplash.client.UnsplashClient;
import com.likelion.picklbe.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyPriceChangeService {

  private final KamisPriceClient kamisPriceClient;
  private final DailyPriceChangeMapper dailyPriceChangeMapper;
  private final UnsplashClient unsplashClient;

  public List<CategoryDailyPriceChangeResponse> getCategoryAverages() {
    List<Item> items =
        Optional.ofNullable(kamisPriceClient.fetchPriceData().getPrice())
            .orElseThrow(() -> new CustomException(DailyPriceChangeErrorCode.PRICE_DATA_NOT_FOUND));

    // 1) 소매/도매별로, 2) 그 안에서 카테고리별로 그룹핑
    Map<String, Map<String, List<Item>>> grouped =
        items.stream()
            .collect(
                Collectors.groupingBy(
                    Item::getProductClsName, Collectors.groupingBy(Item::getCategoryCode)));

    List<CategoryDailyPriceChangeResponse> result = new ArrayList<>();

    for (var clsEntry : grouped.entrySet()) {
      String clsName = clsEntry.getKey(); // "소매" or "도매"
      Map<String, List<Item>> byCategory = clsEntry.getValue();

      for (var catEntry : byCategory.entrySet()) {
        List<Item> groupItems = catEntry.getValue();
        String categoryCode = catEntry.getKey();
        String categoryName = groupItems.get(0).getCategoryName();

        // 가격 파싱
        List<Double> latestPrices =
            groupItems.stream()
                .map(dailyPriceChangeMapper::parseLatestPrice)
                .filter(p -> p > 0)
                .toList();
        List<Double> oneDayAgoPrices =
            groupItems.stream()
                .map(dailyPriceChangeMapper::parseOneDayAgoPrice)
                .filter(p -> p > 0)
                .toList();

        if (latestPrices.isEmpty() || oneDayAgoPrices.isEmpty()) {
          continue;
        }

        double avgLatest = dailyPriceChangeMapper.avg(latestPrices);
        double avgPrev = dailyPriceChangeMapper.avg(oneDayAgoPrices);
        double diff = avgLatest - avgPrev;
        double rate = avgPrev == 0 ? 0 : (diff / avgPrev) * 100;

        result.add(
            CategoryDailyPriceChangeResponse.builder()
                .productClsName(clsName)
                .categoryCode(categoryCode)
                .categoryName(categoryName)
                .avgLatestPrice(Math.round(avgLatest * 100.0) / 100.0)
                .avgOneDayAgoPrice(Math.round(avgPrev * 100.0) / 100.0)
                .priceDiff(Math.round(diff * 100.0) / 100.0)
                .priceDiffRate(Math.round(rate * 100.0) / 100.0)
                .build());
      }
    }

    if (result.isEmpty()) {
      throw new CustomException(DailyPriceChangeErrorCode.PRICE_DATA_NOT_FOUND);
    }
    return result;
  }

  public List<ItemDailyPriceChangeResponse> getItemPrices() {
    var items =
        Optional.ofNullable(kamisPriceClient.fetchPriceData().getPrice())
            .orElseThrow(() -> new CustomException(DailyPriceChangeErrorCode.PRICE_DATA_NOT_FOUND));

    // 1) 기본 가격 응답 만들기
    var base = items.stream().map(dailyPriceChangeMapper::toItemResponse).toList();

    // 2) 같은 품목은 한 번만 검색하도록 요청 단위 캐시
    Map<String, String> imageCache = new HashMap<>();

    // 3) 이미지 URL 붙여서 반환
    return base.stream()
        .map(
            dto -> {
              String query = toUnsplashQuery(dto.getProductName());
              String url =
                  imageCache.computeIfAbsent(query, q -> unsplashClient.searchProduceImageUrl(q));
              return dto.toBuilder().imageUrl(url).build();
            })
        .toList();
  }

  /** "배추/여름(고랭지)" → "배추" 같은 형태로 검색어 정제 */
  private String toUnsplashQuery(String productName) {
    if (productName == null) {
      return "vegetable";
    }
    // 1) '/' 앞쪽 대표명 우선
    String main = productName.split("/", 2)[0];
    // 2) 괄호·특수문자 제거
    main =
        main.replaceAll("[()\\[\\]{}]", " ")
            .replaceAll("[^가-힣a-zA-Z0-9\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    // 3) 최소 폴백
    return main.isEmpty() ? "vegetable" : main;
  }

  public String previewImageUrl(String productName) {
    String query = toUnsplashQuery(productName);
    return unsplashClient.searchProduceImageUrl(query);
  }
}
