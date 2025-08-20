package com.likelion.picklbe.domain.yearlypricechange.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.likelion.picklbe.domain.yearlypricechange.dto.YearlyPriceChangeRawDto;
import com.likelion.picklbe.global.api.kamis.dto.KamisYearlyPriceTrendResponse;

public final class YearlyPriceChangeRawMapper {

  private YearlyPriceChangeRawMapper() {}

  public static YearlyPriceChangeRawDto from(KamisYearlyPriceTrendResponse src) {
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

    List<YearlyPriceChangeRawDto.Row> rows =
        Optional.ofNullable(src.getPrice()).orElse(List.of()).stream()
            .map(
                it ->
                    YearlyPriceChangeRawDto.Row.builder()
                        .yyyy(it.getYyyy())
                        .price(it.getPrice())
                        .max(it.getMax())
                        .min(it.getMin())
                        .build())
            .collect(Collectors.toList());

    return YearlyPriceChangeRawDto.builder().code(code).message(message).rows(rows).build();
  }
}
