package com.likelion.picklbe.global.api.kamis.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.likelion.picklbe.domain.dailypricechange.mapper.DailyPriceChangeMapper;
import com.likelion.picklbe.domain.dailypricechange.response.ItemDailyPriceChangeResponse;
import com.likelion.picklbe.global.api.kamis.client.KamisPriceClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KamisService {

  private final KamisPriceClient kamisPriceClient;
  private final DailyPriceChangeMapper mapper;

  public List<ItemDailyPriceChangeResponse> getAllPriceInfo() {
    return kamisPriceClient.fetchPriceData().getPrice().stream()
        // latestPrice 리스트에 첫 요소가 유효한 경우만
        .filter(
            item -> {
              List<String> lp = item.getLatestPrice();
              return lp != null && !lp.isEmpty() && lp.get(0) != null && !lp.get(0).isBlank();
            })
        // oneDayAgoPrice 리스트에 첫 요소가 유효한 경우만
        .filter(
            item -> {
              List<String> od = item.getOneDayAgoPrice();
              return od != null && !od.isEmpty() && od.get(0) != null && !od.get(0).isBlank();
            })
        .map(mapper::toItemResponse)
        .collect(Collectors.toList());
  }
}
