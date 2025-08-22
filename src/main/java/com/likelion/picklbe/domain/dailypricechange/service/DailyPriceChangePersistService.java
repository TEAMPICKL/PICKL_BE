package com.likelion.picklbe.domain.dailypricechange.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.dailypricechange.entity.KamisCategorySummary;
import com.likelion.picklbe.domain.dailypricechange.entity.KamisItemPrice;
import com.likelion.picklbe.domain.dailypricechange.entity.KamisRawPayload;
import com.likelion.picklbe.domain.dailypricechange.mapper.DailyPriceChangeMapper;
import com.likelion.picklbe.domain.dailypricechange.repository.KamisCategorySummaryRepository;
import com.likelion.picklbe.domain.dailypricechange.repository.KamisItemPriceRepository;
import com.likelion.picklbe.domain.dailypricechange.repository.KamisRawPayloadRepository;
import com.likelion.picklbe.domain.dailypricechange.response.CategoryDailyPriceChangeResponse;
import com.likelion.picklbe.domain.dailypricechange.response.ItemDailyPriceChangeResponse;
import com.likelion.picklbe.global.api.kamis.client.KamisPriceClient;
import com.likelion.picklbe.global.api.kamis.dto.KamisPriceResponse;
import com.likelion.picklbe.global.api.kamis.dto.KamisPriceResponse.Item;
import com.likelion.picklbe.global.api.unsplash.client.UnsplashClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyPriceChangePersistService {

  private final KamisPriceClient kamisPriceClient;
  private final UnsplashClient unsplashClient;
  private final DailyPriceChangeMapper mapper;
  private final KamisRawPayloadRepository rawRepo;
  private final KamisItemPriceRepository itemRepo;
  private final KamisCategorySummaryRepository catRepo;

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private final ObjectMapper objectMapper = new ObjectMapper();

  public record IngestResult(Long rawId, int itemCount, int categoryCount) {}

  // ========= 수집 =========
  @Transactional
  public IngestResult ingestLatest(LocalDate priceDateOrNull) {
    LocalDate priceDate = (priceDateOrNull != null) ? priceDateOrNull : LocalDate.now(KST);

    KamisPriceResponse resp = kamisPriceClient.fetchPriceData();
    String payload = toJson(resp);
    String hash = sha256(payload);

    // 원본 저장 (동일 날짜+내용이면 1건만)
    KamisRawPayload raw =
        rawRepo.findByPriceDateOrderByFetchedAtDesc(priceDate).stream()
            .filter(r -> r.getContentHash().equals(hash))
            .findFirst()
            .orElseGet(
                () ->
                    rawRepo.save(
                        KamisRawPayload.builder()
                            .priceDate(priceDate)
                            .fetchedAt(LocalDateTime.now(KST))
                            .contentHash(hash)
                            .payload(payload)
                            .build()));

    List<Item> items = Optional.ofNullable(resp.getPrice()).orElse(List.of());

    // 같은 날짜 전체 삭제 후 재적재
    itemRepo.deleteByPriceDate(priceDate);

    List<KamisItemPrice> itemEntities =
        items.stream()
            .map(it -> toItemEntity(raw, priceDate, it))
            .filter(Objects::nonNull)
            .collect(
                Collectors.toMap(
                    e -> {
                      String base = e.getProductClsName() + "|" + e.getCategoryCode() + "|";
                      if (e.getProductNo() != null && !e.getProductNo().isBlank()) {
                        return base + "PNO:" + e.getProductNo();
                      }
                      return base + "NAME:" + e.getProductName() + "|" + e.getUnit();
                    },
                    e -> e,
                    (a, b) -> a.getLatestPrice() >= b.getLatestPrice() ? a : b))
            .values()
            .stream()
            .toList();

    itemRepo.saveAll(itemEntities);

    // 카테고리 요약(덮어쓰기)
    catRepo.deleteByPriceDate(priceDate);
    List<KamisCategorySummary> catEntities = aggregateCategories(raw, priceDate, items);
    catRepo.saveAll(catEntities);

    return new IngestResult(raw.getId(), itemEntities.size(), catEntities.size());
  }

  // ========= 조회 =========
  @Transactional(readOnly = true)
  public List<ItemDailyPriceChangeResponse> getStoredItems(LocalDate dateOrNull, String clsOpt) {
    LocalDate date = (dateOrNull != null) ? dateOrNull : itemRepo.findLatestPriceDate();
    if (date == null) {
      return List.of();
    }

    List<KamisItemPrice> rows =
        (clsOpt == null || clsOpt.isBlank())
            ? itemRepo.findByPriceDate(date)
            : itemRepo.findByPriceDateAndProductClsName(date, clsOpt);

    rows.sort(Comparator.comparing(KamisItemPrice::getId));
    return rows.stream().map(this::toItemResp).toList();
  }

  @Transactional(readOnly = true)
  public List<ItemDailyPriceChangeResponse> searchStoredItems(
      LocalDate date, String clsOpt, String q) {
    List<KamisItemPrice> rows =
        (clsOpt == null || clsOpt.isBlank())
            ? itemRepo.findByPriceDateAndProductNameContainingIgnoreCase(date, q)
            : itemRepo.findByPriceDateAndProductClsNameAndProductNameContainingIgnoreCase(
                date, clsOpt, q);

    rows.sort(Comparator.comparing(KamisItemPrice::getId));
    return rows.stream().map(this::toItemResp).toList();
  }

  @Transactional(readOnly = true)
  public Optional<ItemDailyPriceChangeResponse> getItemById(Long id) {
    return itemRepo.findById(id).map(this::toItemResp);
  }

  @Transactional(readOnly = true)
  public List<ItemDailyPriceChangeResponse> searchByName(
      LocalDate dateOrNull, String clsOpt, String q) {
    String query = (q == null) ? "" : q.trim();
    if (query.isBlank()) {
      return List.of();
    }

    LocalDate date = (dateOrNull != null) ? dateOrNull : itemRepo.findLatestPriceDate();
    if (date == null) {
      return List.of();
    }

    List<KamisItemPrice> rows =
        (clsOpt == null || clsOpt.isBlank())
            ? itemRepo.findByPriceDateAndProductNameContainingIgnoreCase(date, query)
            : itemRepo.findByPriceDateAndProductClsNameAndProductNameContainingIgnoreCase(
                date, clsOpt, query);

    rows.sort(java.util.Comparator.comparing(KamisItemPrice::getId));
    return rows.stream().map(this::toItemResp).toList();
  }

  @Transactional(readOnly = true)
  public List<ItemDailyPriceChangeResponse> findByProductNo(
      LocalDate date, String clsOpt, String productNo) {
    List<KamisItemPrice> rows =
        (clsOpt == null || clsOpt.isBlank())
            ? itemRepo.findByPriceDateAndProductNo(date, productNo)
            : itemRepo.findByPriceDateAndProductClsNameAndProductNo(date, clsOpt, productNo);

    rows.sort(Comparator.comparing(KamisItemPrice::getId));
    return rows.stream().map(this::toItemResp).toList();
  }

  // ========= 이미지 보충 =========
  @Transactional
  public Map<String, Object> ingestMissingImages(
      Integer batchSize, LocalDate dateOrNull, String market, boolean refresh, Long startId) {

    int size = (batchSize == null ? 50 : Math.min(50, Math.max(1, batchSize)));
    Pageable page = PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "id"));
    LocalDate date = (dateOrNull != null) ? dateOrNull : itemRepo.findLatestPriceDate();

    List<KamisItemPrice> targets = null;
    boolean refreshMode = refresh;

    if (!refresh) {
      if (date == null) {
        // 최신 수집일이 전혀 없다면 startId 필터를 못 씁니다.
        targets = itemRepo.findByImageUrlIsNullOrderByIdAsc(page);
      } else if (market == null || market.isBlank()) {
        targets =
            (startId != null)
                ? itemRepo.findByPriceDateAndIdGreaterThanEqualAndImageUrlIsNullOrderByIdAsc(
                    date, startId, page)
                : itemRepo.findByPriceDateAndImageUrlIsNullOrderByIdAsc(date, page);
      } else {
        targets =
            (startId != null)
                ? itemRepo
                    .findByPriceDateAndProductClsNameAndIdGreaterThanEqualAndImageUrlIsNullOrderByIdAsc(
                        date, market, startId, page)
                : itemRepo.findByPriceDateAndProductClsNameAndImageUrlIsNullOrderByIdAsc(
                    date, market, page);
      }
      if (targets.isEmpty()) {
        refreshMode = true;
      }
    }

    if (refreshMode) {
      if (date == null) {
        targets = itemRepo.findAllByOrderByIdAsc(page);
      } else if (market == null || market.isBlank()) {
        targets =
            (startId != null)
                ? itemRepo.findByPriceDateAndIdGreaterThanEqualOrderByIdAsc(date, startId, page)
                : itemRepo.findByPriceDateOrderByIdAsc(date, page);
      } else {
        targets =
            (startId != null)
                ? itemRepo.findByPriceDateAndProductClsNameAndIdGreaterThanEqualOrderByIdAsc(
                    date, market, startId, page)
                : itemRepo.findByPriceDateAndProductClsNameOrderByIdAsc(date, market, page);
      }
    }

    if (targets == null || targets.isEmpty()) {
      Map<String, Object> out = new LinkedHashMap<>();
      out.put("processed", 0);
      out.put("updated", 0);
      out.put("skipped", 0);
      out.put("unchanged", 0);
      out.put("date", date);
      out.put("market", market);
      out.put("refresh", refreshMode);
      return out;
    }

    Map<String, String> queryCache = new HashMap<>();
    int updated = 0, skipped = 0, unchanged = 0;

    for (KamisItemPrice e : targets) {
      String key = e.getProductName();
      String url = queryCache.get(key);
      if (url == null) {
        url =
            refreshMode
                ? unsplashClient.searchFirstImageUrl(key)
                : unsplashClient.searchProduceImageUrl(key);
        queryCache.put(key, url);
      }
      if (url == null || url.isBlank()) {
        skipped++;
        continue;
      }
      if (e.getImageUrl() == null || !e.getImageUrl().equals(url)) {
        e.setImageUrl(url);
        updated++;
      } else {
        unchanged++;
      }
    }

    itemRepo.saveAll(targets);

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("processed", targets.size());
    out.put("updated", updated);
    out.put("skipped", skipped);
    out.put("unchanged", unchanged);
    out.put("date", date);
    out.put("market", market);
    out.put("refresh", refreshMode);
    out.put("firstId", targets.get(0).getId());
    out.put("lastId", targets.get(targets.size() - 1).getId());
    return out;
  }

  @Transactional(readOnly = true)
  public List<CategoryDailyPriceChangeResponse> getStoredCategories(
      LocalDate dateOrNull, String clsOpt, String categoryCodeOpt) {

    LocalDate date = (dateOrNull != null) ? dateOrNull : itemRepo.findLatestPriceDate();
    if (date == null) {
      return List.of();
    }

    List<KamisCategorySummary> rows;
    if (clsOpt != null
        && !clsOpt.isBlank()
        && categoryCodeOpt != null
        && !categoryCodeOpt.isBlank()) {
      // 시장 + 카테고리코드
      rows = catRepo.findByPriceDateAndProductClsNameAndCategoryCode(date, clsOpt, categoryCodeOpt);
    } else if (clsOpt != null && !clsOpt.isBlank()) {
      // 시장만
      rows = catRepo.findByPriceDateAndProductClsName(date, clsOpt);
    } else if (categoryCodeOpt != null && !categoryCodeOpt.isBlank()) {
      // 카테고리코드만
      rows = catRepo.findByPriceDateAndCategoryCode(date, categoryCodeOpt);
    } else {
      // 전체
      rows = catRepo.findByPriceDate(date);
    }

    return rows.stream()
        .map(
            r ->
                CategoryDailyPriceChangeResponse.builder()
                    .productClsName(r.getProductClsName())
                    .categoryCode(r.getCategoryCode())
                    .categoryName(r.getCategoryName())
                    .avgLatestPrice(round(r.getAvgLatestPrice()))
                    .avgOneDayAgoPrice(round(r.getAvgOneDayAgoPrice()))
                    .priceDiff(round(r.getPriceDiff()))
                    .priceDiffRate(round(r.getPriceDiffRate()))
                    .build())
        .toList();
  }

  @Transactional(readOnly = true)
  public Optional<KamisRawPayload> getLatestRaw(LocalDate date) {
    return rawRepo.findFirstByPriceDateOrderByFetchedAtDesc(date);
  }

  private ItemDailyPriceChangeResponse toItemResp(KamisItemPrice r) {
    return ItemDailyPriceChangeResponse.builder()
        .id(r.getId())
        .productNo(r.getProductNo())
        .productName(r.getProductName())
        .unit(r.getUnit())
        .latestPrice(round(r.getLatestPrice()))
        .oneDayAgoPrice(round(r.getOneDayAgoPrice()))
        .priceDiff(round(r.getPriceDiff()))
        .priceDiffRate(round(r.getPriceDiffRate()))
        .imageUrl(r.getImageUrl())
        .build();
  }

  // ========= 내부 유틸 =========
  // ========= 내부 유틸 =========
  private KamisItemPrice toItemEntity(KamisRawPayload raw, LocalDate date, Item it) {
    double latest = mapper.parseLatestPrice(it);
    double prev = mapper.parseOneDayAgoPrice(it);
    if (latest <= 0 && prev <= 0) {
      return null;
    }

    String unit = (it.getUnit() == null) ? "" : it.getUnit().trim();
    double diff = latest - prev;
    double rate = (prev == 0) ? 0 : (diff / prev) * 100.0;

    String name = firstNonBlank(it.getProductName(), it.getItemName());
    String pno = nz(it.getProductNo());

    // 이름 → 코드 보정 (일별에는 코드가 없을 수 있음)
    String clsName = nz(it.getProductClsName()); // "소매"/"도매"
    String clsCode = nz(it.getProductClsCode()); // "01"/"02" 있으면 사용
    if (clsCode.isBlank()) {
      if ("소매".equals(clsName)) {
        clsCode = "01";
      } else if ("도매".equals(clsName)) {
        clsCode = "02";
      } else {
        clsCode = null; // 모호하면 null
      }
    }

    // ⬇ 일별 응답에는 없음 → null 로 저장
    String itemCode = null;
    String kindCode = null;
    String gradeRank = null;
    String countyCode = null;

    return KamisItemPrice.builder()
        .raw(raw)
        .priceDate(date)
        .productClsName(clsName)
        .productClsCode(blankToNull(clsCode)) // 있으면 저장
        .categoryCode(nz(it.getCategoryCode()))
        .categoryName(nz(it.getCategoryName()))
        .productName(nz(name))
        .productNo(pno.isBlank() ? null : pno)
        .unit(unit)
        .latestPrice(latest)
        .oneDayAgoPrice(prev)
        .priceDiff(diff)
        .priceDiffRate(rate)
        .itemCode(itemCode) // ← null
        .kindCode(kindCode) // ← null
        .gradeRank(gradeRank) // ← null
        .countyCode(countyCode) // ← null
        .build();
  }

  private List<KamisCategorySummary> aggregateCategories(
      KamisRawPayload raw, LocalDate date, List<Item> items) {
    Map<String, Map<String, List<Item>>> grouped =
        items.stream()
            .collect(
                Collectors.groupingBy(
                    Item::getProductClsName, Collectors.groupingBy(Item::getCategoryCode)));

    List<KamisCategorySummary> out = new ArrayList<>();
    for (var clsEntry : grouped.entrySet()) {
      String cls = nz(clsEntry.getKey());
      Map<String, List<Item>> byCat = clsEntry.getValue();
      for (var catEntry : byCat.entrySet()) {
        List<Item> group = catEntry.getValue();
        if (group.isEmpty()) {
          continue;
        }

        String catCode = nz(catEntry.getKey());
        String catName = nz(group.get(0).getCategoryName());

        List<Double> latest =
            group.stream().map(mapper::parseLatestPrice).filter(p -> p > 0).toList();
        List<Double> prev =
            group.stream().map(mapper::parseOneDayAgoPrice).filter(p -> p > 0).toList();
        if (latest.isEmpty() || prev.isEmpty()) {
          continue;
        }

        double avgLatest = avg(latest);
        double avgPrev = avg(prev);
        double diff = avgLatest - avgPrev;
        double rate = (avgPrev == 0) ? 0 : (diff / avgPrev) * 100.0;

        out.add(
            KamisCategorySummary.builder()
                .raw(raw)
                .priceDate(date)
                .productClsName(cls)
                .categoryCode(catCode)
                .categoryName(catName)
                .avgLatestPrice(avgLatest)
                .avgOneDayAgoPrice(avgPrev)
                .priceDiff(diff)
                .priceDiffRate(rate)
                .build());
      }
    }
    return out;
  }

  private String toJson(Object o) {
    try {
      return objectMapper.writeValueAsString(o);
    } catch (Exception e) {
      return "{\"error\":\"serialization_failed\"}";
    }
  }

  private static String sha256(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : dig) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      return UUID.randomUUID().toString().replace("-", "");
    }
  }

  private static String nz(String s) {
    return (s == null) ? "" : s.trim();
  }

  private static String blankToNull(String s) {
    return (s == null || s.isBlank()) ? null : s;
  }

  private static double round(double v) {
    return Math.round(v * 100.0) / 100.0;
  }

  private static double avg(List<Double> list) {
    return list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
  }

  private static String firstNonBlank(String... xs) {
    for (String x : xs) {
      if (x != null && !x.isBlank()) {
        return x.trim();
      }
    }
    return "";
  }
}
