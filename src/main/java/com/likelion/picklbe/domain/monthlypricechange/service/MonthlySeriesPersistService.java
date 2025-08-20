package com.likelion.picklbe.domain.monthlypricechange.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
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
import com.likelion.picklbe.domain.monthlypricechange.entity.MonthlyCategorySummary;
import com.likelion.picklbe.domain.monthlypricechange.entity.MonthlyItemPrice;
import com.likelion.picklbe.domain.monthlypricechange.repository.MonthlyCategorySummaryRepository;
import com.likelion.picklbe.domain.monthlypricechange.repository.MonthlyItemPriceRepository;
import com.likelion.picklbe.global.api.kamis.client.KamisPriceClient;
import com.likelion.picklbe.global.api.kamis.dto.KamisMonthlyPriceTrendResponse;
import com.likelion.picklbe.global.api.kamis.dto.KamisMonthlyPriceTrendResponse.Price;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlySeriesPersistService {

  private final KamisPriceClient kamisClient;
  private final KamisItemPriceRepository dailyItemRepo; // 메타 추출용
  private final MonthlyItemPriceRepository monthlyItemRepo;
  private final MonthlyCategorySummaryRepository monthlyCatRepo;

  private static final List<String> CATS = List.of("100", "200", "300", "400", "500", "600");
  private static final List<String> CLASSES = List.of("소매", "도매");

  // ===== 저장 =====
  @Transactional
  public Map<String, Object> ingestAll(LocalDate refDate, String market) {
    // 기준월: refDate 또는 Daily 최신일
    LocalDate resolved =
        (refDate != null)
            ? refDate
            : Optional.ofNullable(dailyItemRepo.findLatestPriceDate()).orElse(LocalDate.now());

    // 12개월 yyyymm 리스트 (과거 11개월 ~ 기준월)
    YearMonth end = YearMonth.from(resolved);
    List<String> months = new ArrayList<>();
    for (int i = 11; i >= 0; i--) {
      months.add(end.minusMonths(i).toString().replace("-", "")); // yyyymm
    }

    // productNo 대상
    var productNoPage =
        dailyItemRepo.findDistinctProductNo(
            resolved, market, org.springframework.data.domain.Pageable.unpaged());
    var productNos = productNoPage.getContent();
    log.info(
        "[MONTHLY][INGEST] resolvedMonth={}, market={}, productNos={}",
        end,
        market,
        productNos.size());

    // 기존 삭제
    if (!productNos.isEmpty()) {
      monthlyItemRepo.deleteByProductNoInAndYyyymmIn(productNos, months);
    }

    int savedItemRows = 0;
    Map<String, Meta> metaCache = new HashMap<>();

    // 품목별 월 시계열 수집
    for (String pno : productNos) {
      try {
        KamisMonthlyPriceTrendResponse resp = kamisClient.fetchMonthlyTrend(pno, resolved);
        List<Price> prices = (resp == null) ? null : resp.getPrice();
        if (prices == null || prices.isEmpty()) {
          continue;
        }

        Meta meta = metaCache.computeIfAbsent(pno, k -> loadMeta(resolved, pno));
        List<MonthlyItemPrice> rows =
            prices.stream()
                .map(r -> toEntity(pno, meta, r)) // KamisMonthlyPriceTrendResponse.Price 사용
                .filter(Objects::nonNull)
                .filter(e -> e.getYyyymm() != null && months.contains(e.getYyyymm()))
                .toList();

        if (!rows.isEmpty()) {
          monthlyItemRepo.saveAll(rows);
          savedItemRows += rows.size();
        }
      } catch (Exception e) {
        log.warn("[MONTHLY][INGEST][SKIP] productNo={}, err={}", pno, e.toString());
      }
    }

    // 시장×카테고리 요약
    List<String> clsTargets = (market == null || market.isBlank()) ? CLASSES : List.of(market);
    monthlyCatRepo.deleteByProductClsNameInAndCategoryCodeInAndYyyymmIn(clsTargets, CATS, months);

    int savedCatRows = 0;
    for (String cls : clsTargets) {
      for (String cat : CATS) {
        // 해당 조합 전체 로딩 후 연월별 평균
        var all = monthlyItemRepo.findByProductClsNameAndCategoryCodeOrderByYyyymmAsc(cls, cat);
        if (all.isEmpty()) {
          continue;
        }

        var byYm = all.stream().collect(Collectors.groupingBy(MonthlyItemPrice::getYyyymm));
        for (String ym : months) {
          var bucket = byYm.getOrDefault(ym, List.of());
          if (bucket.isEmpty()) {
            continue;
          }

          double avg =
              bucket.stream()
                  .map(MonthlyItemPrice::getPriceMax)
                  .filter(Objects::nonNull)
                  .mapToDouble(Double::doubleValue)
                  .average()
                  .orElse(0.0);

          String catName = bucket.get(0).getCategoryName();
          monthlyCatRepo.save(
              MonthlyCategorySummary.builder()
                  .productClsName(cls)
                  .categoryCode(cat)
                  .categoryName(catName)
                  .yyyymm(ym)
                  .avgMaxPrice(round2(avg))
                  .build());
          savedCatRows++;
        }
      }
    }

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("refMonth", end.toString()); // yyyy-MM
    out.put("months", months);
    out.put("market", market);
    out.put("savedItemRows", savedItemRows);
    out.put("savedCategoryRows", savedCatRows);
    out.put("productCount", productNos.size());
    return out;
  }

  // ===== 조회: 품목 =====
  @Transactional(readOnly = true)
  public Map<String, Double> getItemSeries(String productNo) {
    return monthlyItemRepo.findByProductNoOrderByYyyymmAsc(productNo).stream()
        .collect(
            Collectors.toMap(
                MonthlyItemPrice::getYyyymm,
                MonthlyItemPrice::getPriceMax,
                (a, b) -> a,
                LinkedHashMap::new));
  }

  @Transactional(readOnly = true)
  public Map<String, Map<String, Double>> getAllItemSeries() {
    List<String> pnos = monthlyItemRepo.findDistinctProductNos();
    if (pnos.isEmpty()) {
      return Map.of();
    }

    Map<String, Map<String, Double>> out = new LinkedHashMap<>();
    var rows = monthlyItemRepo.findByProductNoInOrderByProductNoAscYyyymmAsc(pnos);
    rows.stream()
        .collect(
            Collectors.groupingBy(
                MonthlyItemPrice::getProductNo, LinkedHashMap::new, Collectors.toList()))
        .forEach(
            (pno, list) -> {
              Map<String, Double> series = new LinkedHashMap<>();
              list.forEach(r -> series.put(r.getYyyymm(), r.getPriceMax()));
              out.put(pno, series);
            });
    return out;
  }

  // ===== 조회: 카테고리 =====
  @Transactional(readOnly = true)
  public Map<String, Double> getCategorySeries(String market, String category) {
    return monthlyCatRepo
        .findByProductClsNameAndCategoryCodeOrderByYyyymmAsc(market, category)
        .stream()
        .collect(
            Collectors.toMap(
                MonthlyCategorySummary::getYyyymm,
                MonthlyCategorySummary::getAvgMaxPrice,
                (a, b) -> a,
                LinkedHashMap::new));
  }

  @Transactional(readOnly = true)
  public Map<String, Map<String, Map<String, Double>>> getAllCategorySeries() {
    Map<String, Map<String, Map<String, Double>>> out = new LinkedHashMap<>();
    var rows = monthlyCatRepo.findAllByOrderByProductClsNameAscCategoryCodeAscYyyymmAsc();
    rows.stream()
        .collect(
            Collectors.groupingBy(
                MonthlyCategorySummary::getProductClsName, LinkedHashMap::new, Collectors.toList()))
        .forEach(
            (market, listByMarket) -> {
              Map<String, Map<String, Double>> byCat = new LinkedHashMap<>();
              listByMarket.stream()
                  .collect(
                      Collectors.groupingBy(
                          MonthlyCategorySummary::getCategoryCode,
                          LinkedHashMap::new,
                          Collectors.toList()))
                  .forEach(
                      (cat, list) -> {
                        Map<String, Double> series = new LinkedHashMap<>();
                        list.forEach(r -> series.put(r.getYyyymm(), r.getAvgMaxPrice()));
                        byCat.put(cat, series);
                      });
              out.put(market, byCat);
            });
    return out;
  }

  @Transactional(readOnly = true)
  public Map<String, Map<String, Double>> getAllCategorySeriesForMarket(String market) {
    Map<String, Map<String, Double>> out = new LinkedHashMap<>();
    var rows = monthlyCatRepo.findByProductClsNameOrderByCategoryCodeAscYyyymmAsc(market);
    rows.stream()
        .collect(
            Collectors.groupingBy(
                MonthlyCategorySummary::getCategoryCode, LinkedHashMap::new, Collectors.toList()))
        .forEach(
            (cat, list) -> {
              Map<String, Double> series = new LinkedHashMap<>();
              list.forEach(r -> series.put(r.getYyyymm(), r.getAvgMaxPrice()));
              out.put(cat, series);
            });
    return out;
  }

  // ===== 조회: “행 피벗” (메타 먼저) =====
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getCategorySeriesTable(String marketOpt) {
    var rows =
        (marketOpt == null || marketOpt.isBlank())
            ? monthlyCatRepo.findAllByOrderByProductClsNameAscCategoryCodeAscYyyymmAsc()
            : monthlyCatRepo.findByProductClsNameOrderByCategoryCodeAscYyyymmAsc(marketOpt);
    if (rows.isEmpty()) {
      return List.of();
    }

    var grouped =
        rows.stream()
            .collect(
                Collectors.groupingBy(
                    r -> nz(r.getProductClsName()) + "|" + nz(r.getCategoryCode()),
                    LinkedHashMap::new,
                    Collectors.toList()));

    List<Map<String, Object>> out = new ArrayList<>();
    for (var e : grouped.entrySet()) {
      var list = e.getValue();
      list.sort(Comparator.comparing(MonthlyCategorySummary::getYyyymm));

      var first = list.get(0);
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("productClsName", nz(first.getProductClsName()));
      row.put("categoryCode", nz(first.getCategoryCode()));
      row.put("categoryName", nz(first.getCategoryName()));
      for (var r : list) {
        var ym = r.getYyyymm();
        if (ym == null || ym.isBlank()) {
          continue;
        }
        row.put(ym, r.getAvgMaxPrice());
      }
      out.add(row);
    }
    return out;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getCategorySeriesTableOne(String market, String category) {
    var rows = monthlyCatRepo.findByProductClsNameAndCategoryCodeOrderByYyyymmAsc(market, category);
    if (rows.isEmpty()) {
      return Map.of();
    }

    Map<String, Object> row = new LinkedHashMap<>();
    row.put("productClsName", nz(rows.get(0).getProductClsName()));
    row.put("categoryCode", nz(rows.get(0).getCategoryCode()));
    row.put("categoryName", nz(rows.get(0).getCategoryName()));
    for (var r : rows) {
      var ym = r.getYyyymm();
      if (ym == null || ym.isBlank()) {
        continue;
      }
      row.put(ym, r.getAvgMaxPrice());
    }
    return row;
  }

  // ===== 내부 =====
  private record Meta(
      String productClsName, String categoryCode, String categoryName, String productName) {}

  private Meta loadMeta(LocalDate date, String productNo) {
    var rows = dailyItemRepo.findByPriceDateAndProductNo(date, productNo);
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

  private MonthlyItemPrice toEntity(String pno, Meta meta, Price r) {
    String ym = nz(r.getYyyymm());
    if (ym.isBlank()) {
      return null; // yyyymm null/blank 방어
    }
    Double max = toDoubleSafe(r.getMax());
    if (max == null) {
      return null; // 값 없으면 스킵
    }

    return MonthlyItemPrice.builder()
        .productNo(pno)
        .productName(meta.productName)
        .productClsName(meta.productClsName)
        .categoryCode(meta.categoryCode)
        .categoryName(meta.categoryName)
        .yyyymm(ym)
        .priceMax(max) // 항상 max 사용
        .build();
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
