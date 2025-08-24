package com.likelion.picklbe.global.api.market.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.likelion.picklbe.global.api.market.dto.VWorldResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketOpenApiClient {

  private final WebClient vworldWebClient;

  @Value("${vworld.key}")
  private String apiKey;

  @Value("${vworld.domain}")
  private String domain;

  @Value("${vworld.data-layer}")
  private String dataLayer;

  public VWorldResponse getMarketsByBbox(
      double minX, double minY, double maxX, double maxY, Integer page, Integer size) {

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("service", "data");
    params.add("request", "GetFeature");
    params.add("data", dataLayer);
    params.add("key", apiKey);
    params.add("domain", domain);
    params.add("format", "JSON");
    params.add("crs", "EPSG:4326");
    params.add("geomFilter", String.format("BOX(%f,%f,%f,%f)", minX, minY, maxX, maxY));
    params.add("size", String.valueOf(size != null ? size : 500));
    if (page != null) params.add("page", String.valueOf(page == 0 ? 1 : page)); // vworld는 1부터

    return vworldWebClient
        .get()
        .uri(
            uriBuilder -> {
              var uri = uriBuilder.queryParams(params).build();
              log.info("[VWORLD] GET {}", uri);
              log.info("[VWORLD] key={}, domain={}, data={}", apiKey, domain, dataLayer);
              return uri;
            })
        .retrieve()
        .onStatus(
            s -> !s.is2xxSuccessful(),
            resp ->
                resp.bodyToMono(String.class)
                    .map(
                        body -> {
                          log.error("[VWORLD] ERROR BODY: {}", body);
                          return new RuntimeException("VWorld error: " + body);
                        }))
        .bodyToMono(String.class) // 원문 문자열로 받고
        .doOnNext(body -> log.info("[VWORLD] RAW: {}", body))
        .map(
            body -> { // 직접 파싱 + status 체크 + 서브트리 매핑
              try {
                ObjectMapper om = new ObjectMapper();
                JsonNode root = om.readTree(body);
                JsonNode resp = root.path("response");

                // 에러 응답 처리
                String status = resp.path("status").asText("");
                if ("ERROR".equalsIgnoreCase(status)) {
                  String code = resp.path("error").path("code").asText();
                  String text = resp.path("error").path("text").asText();
                  throw new RuntimeException("VWorld ERROR " + code + ": " + text);
                }

                // response 서브트리만 DTO로 매핑
                VWorldResponse.Response respDto =
                    om.treeToValue(resp, VWorldResponse.Response.class);
                VWorldResponse out = new VWorldResponse();
                out.setResponse(respDto);
                return out;
              } catch (Exception e) {
                throw new RuntimeException("parse fail: " + e.getMessage(), e);
              }
            })
        .block();
  }
}
