package com.likelion.picklbe.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

  @Bean
  WebClient langchainWebClient(
      WebClient.Builder builder, @Value("${langchain.base-url}") String baseUrl) {
    System.out.println("[LangChain BASE] " + baseUrl);
    return builder
        .baseUrl(baseUrl)
        .filter(
            ExchangeFilterFunction.ofRequestProcessor(
                req -> {
                  System.out.println("[LangChain] " + req.method() + " " + req.url());
                  return Mono.just(req);
                }))
        .filter(
            ExchangeFilterFunction.ofResponseProcessor(
                res -> {
                  if (res.statusCode().isError()) {
                    return res.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(
                            body -> {
                              System.err.println(
                                  "[LangChain][ERROR] status="
                                      + res.statusCode()
                                      + " body="
                                      + body);
                              return Mono.error(
                                  new RuntimeException("Upstream status=" + res.statusCode()));
                            });
                  }
                  return Mono.just(res);
                }))
        .build();
  }
}
