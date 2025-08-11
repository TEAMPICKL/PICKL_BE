package com.likelion.picklbe.global.api.kamis.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.likelion.picklbe.global.api.kamis.dto.KamisPriceResponse;

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

  @Cacheable(cacheNames = "kamisDaily", key = "'items'", unless = "#result == null")
  public KamisPriceResponse fetchPriceData() {
    log.info("[KAMIS MISS] fetching daily price…");
    String uri = "/service/price/xml.do";
    return webClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(uri)
                    .queryParam("action", "dailySalesList")
                    .queryParam("p_cert_key", apiKey)
                    .queryParam("p_cert_id", apiId)
                    .queryParam("p_returntype", "json")
                    .build())
        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
        .exchangeToMono(response -> logResponse(response))
        .block();
  }

  private Mono<KamisPriceResponse> logResponse(ClientResponse response) {
    log.info("Status code: {}", response.statusCode());
    MediaType contentType =
        response.headers().contentType().orElse(MediaType.APPLICATION_OCTET_STREAM);
    log.info("Content-Type: {}", contentType);

    return response
        .bodyToMono(String.class)
        .flatMap(
            body -> {
              log.info("Response body: {}", body);
              try {
                KamisPriceResponse dto =
                    new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(body, KamisPriceResponse.class);
                return Mono.just(dto);
              } catch (Exception e) {
                log.error("Failed to parse KamisPriceResponse", e);
                return Mono.error(e);
              }
            });
  }
}
