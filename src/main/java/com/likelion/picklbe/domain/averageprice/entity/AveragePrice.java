package com.likelion.picklbe.domain.averageprice.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "AveragePrice")
public class AveragePrice {
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
