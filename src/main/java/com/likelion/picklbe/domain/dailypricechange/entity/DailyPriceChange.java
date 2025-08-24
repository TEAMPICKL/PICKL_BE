package com.likelion.picklbe.domain.dailypricechange.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "DailyPriceChange")
public class DailyPriceChange {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "categoryCode", nullable = false)
  private String categoryCode;

  @Column(name = "categoryName", nullable = false)
  private String categoryName;

  @Column(name = "avgLatestPrice", nullable = false)
  private double avgLatestPrice;

  @Column(name = "avgOneDayAgoPrice", nullable = false)
  private double avgOneDayAgoPrice;

  @Column(name = "priceDiff", nullable = false)
  private double priceDiff;

  @Column(name = "priceDiffRate", nullable = false)
  private double priceDiffRate;
}
