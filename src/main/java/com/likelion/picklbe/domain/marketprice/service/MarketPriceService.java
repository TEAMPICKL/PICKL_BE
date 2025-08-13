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

    // 1) KAMIS 아이템
    List<Item> items =
        Optional.ofNullable(kamisPriceClient.fetchPriceData().getPrice()).orElseGet(List::of);

    // 2) names(상품명) 1차 필터 (옵션)
    if (names != null && !names.isEmpty()) {
      Set<String> nameSet = names.stream().map(this::norm).collect(Collectors.toSet());
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
                Collectors.toMap(
                    i -> key(i.getProductName(), i.getUnit()), i -> i, (a, b) -> a // 충돌 시 첫 값 유지
                    ));

    // 4) keys("상품명||단위") 2차 필터 (옵션)
    if (keys != null && !keys.isEmpty()) {
      Set<String> keySet = keys.stream().map(this::toKey).collect(Collectors.toSet());
      unique.keySet().retainAll(keySet); // Map 뷰라서 실제 Map이 걸러짐
    }

    // 5) 수동가 전체 로드 → Map (※ 타입은 MarketPrice!)
    Map<String, MarketPrice> manualMap =
        manualRepo.findAll().stream()
            .collect(
                Collectors.toMap(m -> key(m.getProductName(), m.getUnit()), m -> m, (a, b) -> a));

    // 6) onlyManual=true 면 수동가 있는 것만
    var stream = unique.values().stream();
    if (onlyManual) {
      stream = stream.filter(i -> manualMap.containsKey(key(i.getProductName(), i.getUnit())));
    }

    // 7) 응답 매핑
    return stream
        .map(
            i ->
                mapper.toResponse(
                    i, Optional.ofNullable(manualMap.get(key(i.getProductName(), i.getUnit())))))
        .toList();
  }

  /** 관리자용 수동가 업서트(원하면 사용) */
  @Transactional
  public MarketPrice upsert(String productName, String unit, double market, double mart) {
    return manualRepo
        .findByProductNameAndUnit(productName, unit)
        .map(
            m -> {
              m.setMarketPrice(market);
              m.setSuperMarketPrice(mart);
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
                        .build()));
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
