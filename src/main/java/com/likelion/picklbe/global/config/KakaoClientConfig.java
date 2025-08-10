package com.likelion.picklbe.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class KakaoClientConfig {

  // KakaoClientConfig
  @Bean
  public WebClient kakaoWebClient(@Value("${kakao.rest-api-key}") String key) {
    ExchangeFilterFunction logRequest = ExchangeFilterFunction.ofRequestProcessor(req -> {
      log.info("[KAKAO] -> {} {}", req.method(), req.url());
      return Mono.just(req);
    });
    ExchangeFilterFunction logResponse = ExchangeFilterFunction.ofResponseProcessor(res -> {
      log.info("[KAKAO] <- status={}, headers={}", res.statusCode(), res.headers().asHttpHeaders());
      return Mono.just(res);
    });

    return WebClient.builder()
        .baseUrl("https://dapi.kakao.com")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + key)
        .defaultHeader(HttpHeaders.USER_AGENT, "PickLocal/1.0")
        .filter(logRequest)
        .filter(logResponse)
        .build();
  }
}
