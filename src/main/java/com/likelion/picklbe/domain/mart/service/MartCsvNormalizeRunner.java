package com.likelion.picklbe.domain.mart.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@ConditionalOnProperty(value = "app.geocode.enabled", havingValue = "true", matchIfMissing = false)
public class MartCsvNormalizeRunner implements ApplicationRunner {

  private final MartCsvNormalizer normalizer;

  public MartCsvNormalizeRunner(MartCsvNormalizer normalizer) {
    this.normalizer = normalizer;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    normalizer.run();
  }
}
