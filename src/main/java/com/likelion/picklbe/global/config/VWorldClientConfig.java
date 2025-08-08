package com.likelion.picklbe.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class VWorldClientConfig {

  @Bean
  public WebClient vworldWebClient(@Value("${vworld.base-url}") String baseUrl) {
    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(8 * 1024 * 1024))
            .build();

    return WebClient.builder().baseUrl(baseUrl).exchangeStrategies(strategies).build();
  }
}
