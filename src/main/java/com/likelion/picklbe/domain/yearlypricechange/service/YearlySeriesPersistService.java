package com.likelion.picklbe.domain.yearlypricechange.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.dailypricechange.repository.KamisItemPriceRepository;
import com.likelion.picklbe.domain.yearlypricechange.entity.YearlyCategorySummary;
import com.likelion.picklbe.domain.yearlypricechange.entity.YearlyItemPrice;
import com.likelion.picklbe.domain.yearlypricechange.repository.YearlyCategorySummaryRepository;
import com.likelion.picklbe.domain.yearlypricechange.repository.YearlyItemPriceRepository;
import com.likelion.picklbe.global.api.kamis.client.KamisPriceClient;
import com.likelion.picklbe.global.api.kamis.dto.KamisYearlyPriceTrendResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class YearlySeriesPersistService {

  private final KamisPriceClient kamisClient;
  private final KamisItemPriceRepository itemRepo;
  private final YearlyItemPriceRepository yearlyItemRepo;
  private final YearlyCategorySummaryRepository yearlyCatRepo;

  private static final List<String> CATS = List.of("100", "200", "300", "400", "500", "600");
  private static final List<String> CLASSES = List.of("소매", "도매");

  // ====== 배치 저장 ======
  @Transactional
  public Map<String, Object> ingestAll(LocalDate refDate, String market) {
    LocalDate date =
        (refDate != null)
            ? refDate
            : Optional.ofNullable(itemRepo.findLatestPriceDate()).orElse(LocalDate.now());
    String refYear = String.valueOf(date.getYear());
    List<String> years =
        List.of(
            refYear,
            String.valueOf(date.getYear() - 1),
            String.valueOf(date.getYear() - 2),
            String.valueOf(date.getYear() - 3),
            String.valueOf(date.getYear() - 4));

    var productNoPage =
        itemRepo.findDistinctProductNo(
            date, market, org.springframework.data.domain.Pageable.unpaged());
    var productNos = productNoPage.getContent();
    log.info("[YEARLY][INGEST] date={}, market={}, productNos={}", date, market, productNos.size());

    if (!productNos.isEmpty()) {
      yearlyItemRepo.deleteByProductNoInAndYyyyIn(productNos, years);
    }

    int savedItemRows = 0;
    Map<String, Meta> metaCache = new HashMap<>();

    for (String pno : productNos) {
      try {
        KamisYearlyPriceTrendResponse resp = kamisClient.fetchYearlyTrend(pno, date);
        if (resp == null || resp.getPrice() == null) {
          continue;
        }

        Meta meta = metaCache.computeIfAbsent(pno, k -> loadMeta(date, pno));

        List<YearlyItemPrice> rows =
            resp.getPrice().stream()
                .map(
                    t -> {
                      String y = t.getYyyy();
                      if (y == null || !years.contains(y)) {
                        return null;
                      }
                      Double max = toDoubleSafe(t.getMax());
                      if (max == null) {
                        return null;
                      }
                      return YearlyItemPrice.builder()
                          .productNo(pno)
                          .productName(meta.productName)
                          .productClsName(meta.productClsName)
                          .categoryCode(meta.categoryCode)
                          .categoryName(meta.categoryName)
                          .yyyy(y)
                          .priceMax(max)
                          .build();
                    })
                .filter(Objects::nonNull)
                .toList();

        if (!rows.isEmpty()) {
          yearlyItemRepo.saveAll(rows);
          savedItemRows += rows.size();
        }
      } catch (Exception e) {
        log.warn("[YEARLY][INGEST][SKIP] productNo={} error={}", pno, e.toString());
      }
    }

    List<String> clsTargets = (market == null || market.isBlank()) ? CLASSES : List.of(market);
    yearlyCatRepo.deleteByProductClsNameInAndCategoryCodeInAndYyyyIn(clsTargets, CATS, years);

    int savedCatRows = 0;
    for (String cls : clsTargets) {
      for (String cat : CATS) {
        List<YearlyItemPrice> all =
            yearlyItemRepo.findByProductClsNameAndCategoryCodeOrderByYyyyAsc(cls, cat);
        if (all.isEmpty()) {
          continue;
        }

        Map<String, List<YearlyItemPrice>> byYear =
            all.stream().collect(Collectors.groupingBy(YearlyItemPrice::getYyyy));

        for (String y : years) {
          List<YearlyItemPrice> bucket = byYear.getOrDefault(y, List.of());
          if (bucket.isEmpty()) {
            continue;
          }

          double avg =
              bucket.stream()
                  .map(YearlyItemPrice::getPriceMax)
                  .filter(Objects::nonNull)
                  .mapToDouble(Double::doubleValue)
                  .average()
                  .orElse(0.0);

          String catName = bucket.get(0).getCategoryName();

          yearlyCatRepo.save(
              YearlyCategorySummary.builder()
                  .productClsName(cls)
                  .categoryCode(cat)
                  .categoryName(catName)
                  .yyyy(y)
                  .avgMaxPrice(round2(avg))
                  .build());
          savedCatRows++;
        }
      }
    }

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("refYear", refYear);
    out.put("market", market);
    out.put("savedItemRows", savedItemRows);
    out.put("savedCategoryRows", savedCatRows);
    out.put("productCount", productNos.size());
    return out;
  }

  // ====== 조회: 품목 시계열 ======
  @Transactional(readOnly = true)
  public Map<String, Double> getItemSeries(String productNo) {
    return yearlyItemRepo.findByProductNoOrderByYyyyAsc(productNo).stream()
        .collect(
            Collectors.toMap(
                YearlyItemPrice::getYyyy,
                YearlyItemPrice::getPriceMax,
                (a, b) -> a,
                LinkedHashMap::new));
  }

  // ====== 조회: 시장×카테고리 시계열 ======
  @Transactional(readOnly = true)
  public Map<String, Double> getCategorySeries(String market, String categoryCode) {
    return yearlyCatRepo
        .findByProductClsNameAndCategoryCodeOrderByYyyyAsc(market, categoryCode)
        .stream()
        .collect(
            Collectors.toMap(
                YearlyCategorySummary::getYyyy,
                YearlyCategorySummary::getAvgMaxPrice,
                (a, b) -> a,
                LinkedHashMap::new));
  }

  // ====== 조회: 시장×카테고리 “행 피벗” ======
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getCategorySeriesTable(String marketOpt) {
    List<YearlyCategorySummary> rows =
        (marketOpt == null || marketOpt.isBlank())
            ? yearlyCatRepo.findAllByOrderByProductClsNameAscCategoryCodeAscYyyyAsc()
            : yearlyCatRepo.findByProductClsNameOrderByCategoryCodeAscYyyyAsc(marketOpt);

    if (rows.isEmpty()) {
      return List.of();
    }

    // 시장|카테고리로 그룹화
    var grouped =
        rows.stream()
            .collect(
                Collectors.groupingBy(
                    r -> nz(r.getProductClsName()) + "|" + nz(r.getCategoryCode()),
                    LinkedHashMap::new,
                    Collectors.toList()));

    List<Map<String, Object>> out = new java.util.ArrayList<>();
    for (var entry : grouped.entrySet()) {
      var list = entry.getValue();
      list.sort(Comparator.comparing(YearlyCategorySummary::getYyyy));

      var first = list.get(0);
      Map<String, Object> row = new LinkedHashMap<>();

      // ① 메타 먼저
      row.put("productClsName", nz(first.getProductClsName()));
      row.put("categoryCode", nz(first.getCategoryCode()));
      row.put("categoryName", nz(first.getCategoryName()));

      // ② 연도 필드들 (삽입 순서 그대로 출력)
      for (var r : list) {
        String yyyy = r.getYyyy();
        if (yyyy == null || yyyy.isBlank()) {
          continue;
        }
        row.put(yyyy, r.getAvgMaxPrice());
      }
      out.add(row);
    }
    return out;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getCategorySeriesTableOne(String market, String categoryCode) {
    var rows =
        yearlyCatRepo.findByProductClsNameAndCategoryCodeOrderByYyyyAsc(market, categoryCode);
    if (rows.isEmpty()) {
      return Map.of();
    }

    var first = rows.get(0);
    Map<String, Object> row = new LinkedHashMap<>();
    // ① 메타 먼저
    row.put("productClsName", nz(first.getProductClsName()));
    row.put("categoryCode", nz(first.getCategoryCode()));
    row.put("categoryName", nz(first.getCategoryName()));
    // ② 연도들
    for (var r : rows) {
      String yyyy = r.getYyyy();
      if (yyyy == null || yyyy.isBlank()) {
        continue;
      }
      row.put(yyyy, r.getAvgMaxPrice());
    }
    return row;
  }

  // ====== 조회: 전체 ======
  @Transactional(readOnly = true)
  public Map<String, Map<String, Double>> getAllItemSeries() {
    List<String> allProductNos = yearlyItemRepo.findDistinctProductNos();
    if (allProductNos.isEmpty()) {
      return Map.of();
    }

    Map<String, Map<String, Double>> out = new LinkedHashMap<>();
    var rows = yearlyItemRepo.findByProductNoInOrderByProductNoAscYyyyAsc(allProductNos);
    rows.stream()
        .collect(
            Collectors.groupingBy(
                YearlyItemPrice::getProductNo, LinkedHashMap::new, Collectors.toList()))
        .forEach(
            (pno, list) -> {
              Map<String, Double> series = new LinkedHashMap<>();
              list.forEach(r -> series.put(r.getYyyy(), r.getPriceMax()));
              out.put(pno, series);
            });
    return out;
  }

  @Transactional(readOnly = true)
  public Map<String, Map<String, Map<String, Double>>> getAllCategorySeries() {
    Map<String, Map<String, Map<String, Double>>> out = new LinkedHashMap<>();
    var rows = yearlyCatRepo.findAllByOrderByProductClsNameAscCategoryCodeAscYyyyAsc();
    rows.stream()
        .collect(
            Collectors.groupingBy(
                YearlyCategorySummary::getProductClsName, LinkedHashMap::new, Collectors.toList()))
        .forEach(
            (market, listByMarket) -> {
              Map<String, Map<String, Double>> byCategory = new LinkedHashMap<>();
              listByMarket.stream()
                  .collect(
                      Collectors.groupingBy(
                          YearlyCategorySummary::getCategoryCode,
                          LinkedHashMap::new,
                          Collectors.toList()))
                  .forEach(
                      (cat, list) -> {
                        Map<String, Double> series = new LinkedHashMap<>();
                        list.forEach(r -> series.put(r.getYyyy(), r.getAvgMaxPrice()));
                        byCategory.put(cat, series);
                      });
              out.put(market, byCategory);
            });
    return out;
  }

  @Transactional(readOnly = true)
  public Map<String, Map<String, Double>> getAllCategorySeriesForMarket(String market) {
    Map<String, Map<String, Double>> out = new LinkedHashMap<>();
    var rows = yearlyCatRepo.findByProductClsNameOrderByCategoryCodeAscYyyyAsc(market);
    rows.stream()
        .collect(
            Collectors.groupingBy(
                YearlyCategorySummary::getCategoryCode, LinkedHashMap::new, Collectors.toList()))
        .forEach(
            (cat, list) -> {
              Map<String, Double> series = new LinkedHashMap<>();
              list.forEach(r -> series.put(r.getYyyy(), r.getAvgMaxPrice()));
              out.put(cat, series);
            });
    return out;
  }

  // ====== 내부 유틸 ======

  private record Meta(
      String productClsName, String categoryCode, String categoryName, String productName) {}

  private Meta loadMeta(LocalDate date, String productNo) {
    var rows = itemRepo.findByPriceDateAndProductNo(date, productNo);
    if (rows == null || rows.isEmpty()) {
      return new Meta(null, null, null, null);
    }
    var r = rows.get(0);
    return new Meta(
        nz(r.getProductClsName()),
        nz(r.getCategoryCode()),
        nz(r.getCategoryName()),
        nz(r.getProductName()));
  }

  private static Double toDoubleSafe(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    try {
      return Double.parseDouble(s.trim());
    } catch (Exception e) {
      return null;
    }
  }

  private static String nz(String s) {
    return (s == null) ? "" : s.trim();
  }

  private static double round2(double v) {
    return Math.round(v * 100.0) / 100.0;
  }
}
