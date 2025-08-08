package com.likelion.picklbe.domain.marketplace.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketMarkerResponse {
  private String id;
  private String name;
  private String category;
  private String address;
  private Double lat;
  private Double lng;
  private Boolean parking;
}
