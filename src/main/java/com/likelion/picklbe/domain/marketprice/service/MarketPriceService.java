package com.likelion.picklbe.domain.marketprice.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.marketprice.dto.response.MarketPriceResponse;
import com.likelion.picklbe.domain.marketprice.entity.MarketPrice;
import com.likelion.picklbe.domain.marketprice.mapper.MarketPriceMapper;
import com.likelion.picklbe.domain.marketprice.repository.MarketPriceRepository;
import com.likelion.picklbe.global.api.kamis.client.KamisPriceClient;
import com.likelion.picklbe.global.api.kamis.dto.KamisPriceResponse.Item;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketPriceService {

  private final KamisPriceClient kamisPriceClient;
  private final MarketPriceRepository manualRepo;
  private final MarketPriceMapper mapper;

  /** 기본 리스트: 필터 없이 호출 */
  @Transactional(readOnly = true)
  public List<MarketPriceResponse> getMarketPrices() {
    return getMarketPricesFiltered(null, null, false);
  }

  /** 선택 필터 리스트 */
  @Transactional(readOnly = true)
  public List<MarketPriceResponse> getMarketPricesFiltered(
      Set<String> names, Set<String> keys, boolean onlyManual) {

    // 1) KAMIS 원본
    List<Item> items =
        Optional.ofNullable(kamisPriceClient.fetchPriceData().getPrice()).orElseGet(List::of);

    // 2) 선택 필터: names
    Set<String> nameSet =
        (names == null || names.isEmpty())
            ? null
            : names.stream().map(this::norm).collect(Collectors.toSet());
    if (nameSet != null) {
      items =
          items.stream()
              .filter(i -> i.getProductName() != null && nameSet.contains(norm(i.getProductName())))
              .toList();
    }

    // 3) (productName, unit) 기준 유니크
    Map<String, Item> unique =
        items.stream()
            .filter(i -> i.getProductName() != null && i.getUnit() != null)
            .collect(
                Collectors.toMap(i -> key(i.getProductName(), i.getUnit()), i -> i, (a, b) -> a));

    // 4) 선택 필터: keys
    Set<String> keySet =
        (keys == null || keys.isEmpty())
            ? null
            : keys.stream().map(this::toKey).collect(Collectors.toSet());
    if (keySet != null) {
      unique.keySet().retainAll(keySet);
    }

    // 5) 수동가 맵
    Map<String, MarketPrice> manualMap =
        manualRepo.findAll().stream()
            .collect(
                Collectors.toMap(m -> key(m.getProductName(), m.getUnit()), m -> m, (a, b) -> a));

    List<MarketPriceResponse> out = new java.util.ArrayList<>();

    // 6) KAMIS 기반 매핑 (onlyManual이면 manual 있는 키만)
    for (Item i : unique.values()) {
      String k = key(i.getProductName(), i.getUnit());
      if (onlyManual && !manualMap.containsKey(k)) {
        continue;
      }
      out.add(mapper.toResponse(i, Optional.ofNullable(manualMap.get(k))));
    }

    // 7) ✅ manual-only(=KAMIS에 없는 키)도 포함
    if (onlyManual) {
      for (MarketPrice m : manualMap.values()) {
        String k = key(m.getProductName(), m.getUnit());
        if (unique.containsKey(k)) {
          continue; // 이미 위에서 포함됨
        }
        if (nameSet != null && !nameSet.contains(norm(m.getProductName()))) {
          continue;
        }
        if (keySet != null && !keySet.contains(k)) {
          continue; // 키 필터가 있으면 존중
        }
        out.add(mapper.fromManual(m));
      }
    }

    return out;
  }

  /** 관리자용 수동가 업서트(원하면 사용) */
  @Transactional
  public MarketPrice upsert(
      String productName,
      String unit,
      double market,
      double mart,
      String imageUrl,
      String productNo) {

    return manualRepo
        .findByProductNameAndUnit(productName, unit)
        .map(
            m -> {
              m.setMarketPrice(market);
              m.setSuperMarketPrice(mart);
              if (imageUrl != null && !imageUrl.isBlank()) {
                m.setImageUrl(imageUrl);
              }
              if (productNo != null && !productNo.isBlank()) {
                m.setProductNo(productNo);
              }
              return m;
            })
        .orElseGet(
            () ->
                manualRepo.save(
                    MarketPrice.builder()
                        .productName(productName)
                        .unit(unit)
                        .marketPrice(market)
                        .superMarketPrice(mart)
                        .imageUrl(imageUrl == null ? "" : imageUrl)
                        .productNo(productNo)
                        .build()));
  }

  @Transactional
  public MarketPrice patch(
      Long id,
      String productName,
      String unit,
      Double marketPrice,
      Double superMarketPrice,
      String imageUrl,
      String productNo) {
    MarketPrice m =
        manualRepo
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("market_price not found: id=" + id));

    if (productName != null) {
      m.setProductName(productName);
    }
    if (unit != null) {
      m.setUnit(unit);
    }
    if (marketPrice != null) {
      m.setMarketPrice(marketPrice);
    }
    if (superMarketPrice != null) {
      m.setSuperMarketPrice(superMarketPrice);
    }
    if (imageUrl != null) {
      m.setImageUrl(imageUrl);
    }
    if (productNo != null) {
      m.setProductNo(productNo);
    }

    return manualRepo.save(m);
  }

  /** 내부 키: "상품명||단위" (공백 정규화 포함) */
  private String key(String productName, String unit) {
    return norm(productName) + "||" + norm(unit);
  }

  /** 공백 정규화 */
  private String norm(String s) {
    return s == null ? "" : s.trim().replaceAll("\\s+", " ");
  }

  /** 외부 쿼리파라미터를 내부 키로 정규화 */
  private String toKey(String raw) {
    if (raw == null) {
      return "";
    }
    String s = raw.trim();
    String[] parts = s.split("\\|\\|", 2);
    if (parts.length == 2) {
      return key(parts[0], parts[1]);
    }
    parts = s.split("\\|", 2); // 백업: 하나짜리 파이프 허용
    if (parts.length == 2) {
      return key(parts[0], parts[1]);
    }
    return s; // 단위 생략 입력이면 원문 반환(실제 retainAll 시 매치 안 됨)
  }
}
