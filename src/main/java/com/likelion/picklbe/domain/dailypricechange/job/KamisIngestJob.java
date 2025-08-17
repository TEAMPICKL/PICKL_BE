package com.likelion.picklbe.domain.dailypricechange.job;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.likelion.picklbe.domain.dailypricechange.service.DailyPriceChangePersistService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KamisIngestJob {

  private final DailyPriceChangePersistService service;
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  // 매일 09:10 KST 수집 (필요에 따라 조정)
  @Scheduled(cron = "0 10 9 * * *", zone = "Asia/Seoul")
  public void ingestDaily() {
    service.ingestLatest(LocalDate.now(KST));
  }
}
