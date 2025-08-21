package com.likelion.picklbe.domain.mart.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@ConditionalOnProperty(
    value = "app.place-loader.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class PlaceCsvLoaderRunner implements ApplicationRunner {

  private final PlaceCsvLoader loader;

  public PlaceCsvLoaderRunner(PlaceCsvLoader loader) {
    this.loader = loader;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    loader.run();
  }
}
