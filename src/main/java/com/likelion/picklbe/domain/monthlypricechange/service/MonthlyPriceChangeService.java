package com.likelion.picklbe.domain.monthlypricechange.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.likelion.picklbe.domain.dailypricechange.repository.KamisItemPriceRepository;
import com.likelion.picklbe.domain.monthlypricechange.dto.MonthlyPriceChangeRawDto;
import com.likelion.picklbe.domain.monthlypricechange.mapper.MonthlyPriceChangeRawMapper;
import com.likelion.picklbe.global.api.kamis.client.KamisPriceClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyPriceChangeService {

  private static final int MAX_LIMIT = 200;

  private final KamisPriceClient kamisPriceClient;
  private final KamisItemPriceRepository itemRepo;
  private final ObjectMapper objectMapper = new ObjectMapper();

  /** 배치 조회: latest priceDate 기준으로 productNo를 뽑아서 순회 */
  // 기존 MonthlyPriceChangeService 안에 적용 (핵심만 발췌)
  public Map<String, MonthlyPriceChangeRawDto> getRawBatch(LocalDate dateOrNull, String market) {

    LocalDate date = (dateOrNull != null) ? dateOrNull : itemRepo.findLatestPriceDate();
    if (date == null) {
      date = LocalDate.now();
    }

    log.info("[MONTHLY][BATCH] resolved date={}, market={}", date, market);

    // ✅ 전체 productNo를 한번에 조회 (리포지토리 메서드가 Page<T>만 있으면 Pageable.unpaged() 사용)
    var productNoPage =
        itemRepo.findDistinctProductNo(
            date, market, org.springframework.data.domain.Pageable.unpaged());
    var productNos = productNoPage.getContent();

    log.info(
        "[MONTHLY][BATCH] productNos fetched: {} (sample: {})",
        productNos.size(),
        productNos.stream().limit(10).toList());

    Map<String, MonthlyPriceChangeRawDto> out = new LinkedHashMap<>();
    for (String pno : productNos) {
      try {
        var resp = kamisPriceClient.fetchMonthlyTrend(pno, date); // p_regday=yyyy-MM-dd or yyyy-MM
        var dto = MonthlyPriceChangeRawMapper.from(resp);

        int rows = (dto.getRows() == null) ? 0 : dto.getRows().size();
        if (rows == 0) {
          log.warn(
              "[MONTHLY][BATCH] EMPTY rows -> productNo={}, date={}, code={}, message={}",
              pno,
              date,
              dto.getCode(),
              dto.getMessage());
        } else {
          log.info(
              "[MONTHLY][BATCH] OK -> productNo={}, rows={}, firstYyyymm={}",
              pno,
              rows,
              dto.getRows().get(0).getYyyymm());
        }
        out.put(pno, dto);
      } catch (Exception e) {
        log.error("[MONTHLY][BATCH] error on productNo={}, date={}", pno, date, e);
      }
    }

    log.info("[MONTHLY][BATCH] done. result size={}", out.size());
    return out;
  }
}
