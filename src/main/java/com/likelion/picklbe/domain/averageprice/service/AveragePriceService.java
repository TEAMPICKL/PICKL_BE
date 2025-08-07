package com.likelion.picklbe.domain.averageprice.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.likelion.picklbe.domain.averageprice.exception.AveragePriceErrorCode;
import com.likelion.picklbe.domain.averageprice.mapper.AveragePriceMapper;
import com.likelion.picklbe.domain.averageprice.response.CategoryAveragePriceResponse;
import com.likelion.picklbe.domain.averageprice.response.ItemPriceResponse;
import com.likelion.picklbe.global.api.kamis.client.KamisPriceClient;
import com.likelion.picklbe.global.api.kamis.dto.KamisPriceResponse.Item;
import com.likelion.picklbe.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AveragePriceService {

  private final KamisPriceClient kamisPriceClient;
  private final AveragePriceMapper averagePriceMapper;

  public List<CategoryAveragePriceResponse> getCategoryAverages() {
    List<Item> items =
        Optional.ofNullable(kamisPriceClient.fetchPriceData().getPrice())
            .orElseThrow(() -> new CustomException(AveragePriceErrorCode.PRICE_DATA_NOT_FOUND));

    // 1) 소매/도매별로, 2) 그 안에서 카테고리별로 그룹핑
    Map<String, Map<String, List<Item>>> grouped =
        items.stream()
            .collect(
                Collectors.groupingBy(
                    Item::getProductClsName, Collectors.groupingBy(Item::getCategoryCode)));

    List<CategoryAveragePriceResponse> result = new ArrayList<>();

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
                .map(averagePriceMapper::parseLatestPrice)
                .filter(p -> p > 0)
                .toList();
        List<Double> oneDayAgoPrices =
            groupItems.stream()
                .map(averagePriceMapper::parseOneDayAgoPrice)
                .filter(p -> p > 0)
                .toList();

        if (latestPrices.isEmpty() || oneDayAgoPrices.isEmpty()) {
          continue;
        }

        double avgLatest = averagePriceMapper.avg(latestPrices);
        double avgPrev = averagePriceMapper.avg(oneDayAgoPrices);
        double diff = avgLatest - avgPrev;
        double rate = avgPrev == 0 ? 0 : (diff / avgPrev) * 100;

        result.add(
            CategoryAveragePriceResponse.builder()
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
      throw new CustomException(AveragePriceErrorCode.PRICE_DATA_NOT_FOUND);
    }
    return result;
  }

  public List<ItemPriceResponse> getItemPrices() {
    List<Item> items =
        Optional.ofNullable(kamisPriceClient.fetchPriceData().getPrice())
            .orElseThrow(() -> new CustomException(AveragePriceErrorCode.PRICE_DATA_NOT_FOUND));

    return items.stream().map(averagePriceMapper::toItemResponse).toList();
  }
}
