package com.likelion.picklbe.global.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Configuration
public class KakaoClientConfig {

  @Bean
  public WebClient kakaoWebClient(@Value("${kakao.rest-api-key}") String key) {
    // 1) 키 검증 + 마스킹 로깅
    if (StringUtils.isBlank(key)) {
      log.error("[KAKAO] REST API Key 가 비어 있습니다. application-*.properties 또는 환경변수를 확인하세요.");
      throw new IllegalStateException("Kakao REST API Key is missing");
    }
    String masked =
        key.length() <= 8 ? "****" : key.substring(0, 6) + "..." + key.substring(key.length() - 2);
    log.info("[KAKAO] Using REST API Key (masked): {}", masked);

    // 2) Netty 타임아웃/압축 설정
    HttpClient httpClient =
        HttpClient.create()
            .compress(true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000) // connect timeout 3s
            .responseTimeout(Duration.ofSeconds(10)); // response timeout 10s

    // 3) 요청/응답 로깅 필터
    ExchangeFilterFunction logRequest =
        ExchangeFilterFunction.ofRequestProcessor(
            req -> {
              log.info("[KAKAO] -> {} {}", req.method(), req.url());
              return Mono.just(req);
            });
    ExchangeFilterFunction logResponse =
        ExchangeFilterFunction.ofResponseProcessor(
            res -> {
              log.info(
                  "[KAKAO] <- status={}, headers={}",
                  res.statusCode(),
                  res.headers().asHttpHeaders());
              return Mono.just(res);
            });

    // 4) WebClient 빌드
    return WebClient.builder()
        .baseUrl("https://dapi.kakao.com")
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + key)
        .defaultHeader(HttpHeaders.USER_AGENT, "PickLocal/1.0")
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .filter(logRequest)
        .filter(logResponse)
        .build();
  }
}
