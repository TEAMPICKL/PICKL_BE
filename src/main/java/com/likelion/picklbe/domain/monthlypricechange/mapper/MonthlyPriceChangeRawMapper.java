package com.likelion.picklbe.domain.monthlypricechange.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.likelion.picklbe.domain.monthlypricechange.dto.MonthlyPriceChangeRawDto;
import com.likelion.picklbe.global.api.kamis.dto.KamisMonthlyPriceTrendResponse;

public final class MonthlyPriceChangeRawMapper {

  private MonthlyPriceChangeRawMapper() {}

  public static MonthlyPriceChangeRawDto from(KamisMonthlyPriceTrendResponse src) {
    String code =
        Optional.ofNullable(src.getCondition())
            .filter(list -> !list.isEmpty())
            .map(list -> list.get(0).getCode())
            .orElse(null);

    String message =
        Optional.ofNullable(src.getCondition())
            .filter(list -> !list.isEmpty())
            .map(list -> list.get(0).getMessage())
            .orElse(null);

    List<MonthlyPriceChangeRawDto.Row> rows =
        Optional.ofNullable(src.getPrice()).orElse(List.of()).stream()
            .map(
                it ->
                    MonthlyPriceChangeRawDto.Row.builder()
                        .yyyymm(it.getYyyymm())
                        .price(it.getPrice())
                        .max(it.getMax())
                        .min(it.getMin())
                        .build())
            .collect(Collectors.toList());

    return MonthlyPriceChangeRawDto.builder().code(code).message(message).rows(rows).build();
  }
}
