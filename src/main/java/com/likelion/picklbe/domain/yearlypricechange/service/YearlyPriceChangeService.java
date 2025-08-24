// com.likelion.picklbe.domain.yearlypricechange.service.YearlyPriceChangeService

package com.likelion.picklbe.domain.yearlypricechange.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.dailypricechange.repository.KamisItemPriceRepository;
import com.likelion.picklbe.domain.yearlypricechange.dto.YearlyPriceChangeRawDto;
import com.likelion.picklbe.domain.yearlypricechange.mapper.YearlyPriceChangeRawMapper;
import com.likelion.picklbe.global.api.kamis.client.KamisPriceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class YearlyPriceChangeService {

  private final KamisPriceClient kamisPriceClient;
  private final KamisItemPriceRepository itemRepo;

  /** 배치 조회: 특정 기준일의 품목코드 목록을 뽑아 연간 추이를 일괄로 가져옴 */
  @Transactional(readOnly = true)
  public Map<String, YearlyPriceChangeRawDto> getRawBatch(LocalDate dateOrNull, String market) {

    LocalDate date = (dateOrNull != null) ? dateOrNull : itemRepo.findLatestPriceDate();
    if (date == null) {
      date = LocalDate.now();
    }

    log.info("[YEARLY][BATCH] resolved date={}, market={}", date, market);

    // 월간과 동일하게: 전체 productNo를 한 번에 조회
    var productNoPage =
        itemRepo.findDistinctProductNo(
            date, market, org.springframework.data.domain.Pageable.unpaged());
    var productNos = productNoPage.getContent();

    log.info(
        "[YEARLY][BATCH] productNos fetched: {} (sample: {})",
        productNos.size(),
        productNos.stream().limit(10).toList());

    Map<String, YearlyPriceChangeRawDto> out = new LinkedHashMap<>();
    for (String pno : productNos) {
      try {
        var resp = kamisPriceClient.fetchYearlyTrend(pno, date); // p_regday=yyyy 로 변환됨(클라이언트에서 처리)
        var dto = YearlyPriceChangeRawMapper.from(resp);

        int rows = (dto.getRows() == null) ? 0 : dto.getRows().size();
        if (rows == 0) {
          log.warn(
              "[YEARLY][BATCH] EMPTY rows -> productNo={}, date={}, code={}, message={}",
              pno,
              date,
              dto.getCode(),
              dto.getMessage());
        } else {
          // 연도 배열이므로 첫 행은 가장 최근 연도로 가정
          log.info(
              "[YEARLY][BATCH] OK -> productNo={}, rows={}, firstYyyy={}",
              pno,
              rows,
              dto.getRows().get(0).getYyyy());
        }
        out.put(pno, dto);
      } catch (Exception e) {
        log.error("[YEARLY][BATCH] error on productNo={}, date={}", pno, date, e);
      }
    }

    log.info("[YEARLY][BATCH] done. result size={}", out.size());
    return out;
  }
}
