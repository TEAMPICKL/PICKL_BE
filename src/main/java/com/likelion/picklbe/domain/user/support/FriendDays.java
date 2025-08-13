package com.likelion.picklbe.domain.user.support;

import java.time.*;
import java.time.temporal.ChronoUnit;

public final class FriendDays {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private FriendDays() {}

  public static long sinceInclusive(Instant createdAtUtc) {
    LocalDate joined = createdAtUtc.atZone(ZoneId.of("UTC")).withZoneSameInstant(KST).toLocalDate();
    LocalDate today = LocalDate.now(KST);
    return ChronoUnit.DAYS.between(joined, today) + 1; // 오늘 포함
  }
}
