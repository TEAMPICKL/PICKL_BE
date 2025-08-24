package com.likelion.picklbe.global.api.kamis.client;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.likelion.picklbe.global.api.kamis.dto.KamisMonthlyPriceTrendResponse;
import com.likelion.picklbe.global.api.kamis.dto.KamisPriceResponse;
import com.likelion.picklbe.global.api.kamis.dto.KamisYearlyPriceTrendResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KamisPriceClient {

  @Value("${kamis.api.key}")
  private String apiKey;

  @Value("${kamis.api.id}")
  private String apiId;

  private final WebClient webClient =
      WebClient.builder().baseUrl("https://www.kamis.or.kr").build();
  private final ObjectMapper om = new ObjectMapper();

  // ✔ 6번(일자기준) 그대로 사용
  public KamisPriceResponse fetchPriceData() {
    log.info("[KAMIS] fetching daily price…");
    return webClient
        .get()
        .uri(
            b ->
                b.path("/service/price/xml.do")
                    .queryParam("action", "dailySalesList")
                    .queryParam("p_cert_key", apiKey)
                    .queryParam("p_cert_id", apiId)
                    .queryParam("p_returntype", "json")
                    .build())
        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
        .exchangeToMono(res -> parse(res, KamisPriceResponse.class))
        .block();
  }

  // ========= 8번: 월평균 가격추이(상품 기준) =========
  private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // ← 중요!

  public KamisMonthlyPriceTrendResponse fetchMonthlyTrend(String productNo, LocalDate regDay) {
    final String pn = productNo == null ? "" : productNo.trim();
    final String pRegday = (regDay != null) ? regDay.format(YMD) : null; // ← yyyy-MM-dd

    log.info("[KAMIS][MONTHLY] call productno={}, p_regday={}", pn, pRegday);

    return webClient
        .get()
        .uri(
            b -> {
              var u =
                  b.path("/service/price/xml.do")
                      .queryParam("action", "monthlyPriceTrendList")
                      .queryParam("p_cert_key", apiKey)
                      .queryParam("p_cert_id", apiId)
                      .queryParam("p_returntype", "json")
                      // 문서가 혼용하여 표기 → 둘 다 전송해 호환성 확보
                      .queryParam("p_productno", pn)
                      .queryParam("productno", pn);
              if (pRegday != null) {
                u = u.queryParam("p_regday", pRegday);
              }
              return u.build();
            })
        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
        .exchangeToMono(res -> parse(res, KamisMonthlyPriceTrendResponse.class))
        .doOnNext(
            body -> {
              var rows = (body.getPrice() == null) ? 0 : body.getPrice().size();
              var topCode = body.getErrorCode(); // 상단 error_code
              var cond =
                  (body.getCondition() != null && !body.getCondition().isEmpty())
                      ? body.getCondition().get(0)
                      : null;
              log.info(
                  "[KAMIS][MONTHLY] productno={} -> top.error_code={}, rows={}, echo(p_productno={}, p_regday={})",
                  pn,
                  topCode,
                  rows,
                  cond != null ? cond.getPProductNo() : null,
                  cond != null ? cond.getPRegday() : null);
            })
        .block();
  }

  // ========= 연간 추이 =========
  // KamisPriceClient

  public KamisYearlyPriceTrendResponse fetchYearlyTrend(String productNo, LocalDate regDay) {
    final String pn = productNo == null ? "" : productNo.trim();
    final String pRegday = (regDay != null) ? regDay.format(YMD) : null;

    log.info("[KAMIS][YEARLY] call productno={}, p_regday={}", pn, pRegday);

    return webClient
        .get()
        .uri(
            b -> {
              var u =
                  b.path("/service/price/xml.do")
                      .queryParam("action", "yearlyPriceTrendList")
                      .queryParam("p_cert_key", apiKey)
                      .queryParam("p_cert_id", apiId)
                      .queryParam("p_returntype", "json")
                      // ✅ 둘 다 보냄: 일부 환경은 p_productno만, 일부는 productno만 인식
                      .queryParam("p_productno", pn)
                      .queryParam("productno", pn);
              if (pRegday != null) {
                u = u.queryParam("p_regday", pRegday);
              }
              return u.build();
            })
        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
        .exchangeToMono(res -> parse(res, KamisYearlyPriceTrendResponse.class))
        .doOnNext(
            body -> {
              int rows = (body.getPrice() == null) ? 0 : body.getPrice().size();
              var cond =
                  (body.getCondition() != null && !body.getCondition().isEmpty())
                      ? body.getCondition().get(0)
                      : null;
              log.info(
                  "[KAMIS][YEARLY] productno={} -> top.code={}, rows={}, echo(p_productno={}, p_regday={})",
                  pn,
                  body.getTopErrorCode(),
                  rows,
                  cond != null ? cond.getPProductNo() : null,
                  cond != null ? cond.getPRegday() : null);
            })
        .block();
  }

  // 공통 파서
  private <T> Mono<T> parse(ClientResponse res, Class<T> type) {
    log.info(
        "[KAMIS][HTTP] Status={}, Content-Type={}",
        res.statusCode(),
        res.headers().contentType().orElse(MediaType.APPLICATION_OCTET_STREAM));
    return res.bodyToMono(String.class)
        .flatMap(
            body -> {
              log.info("[KAMIS][HTTP] Body: {}", body);
              try {
                return Mono.just(om.readValue(body, type));
              } catch (Exception e) {
                log.error("KAMIS parse error", e);
                return Mono.error(e);
              }
            });
  }
}
