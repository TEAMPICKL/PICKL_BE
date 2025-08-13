// com.likelion.picklbe.domain.marketprice.entity.ManualMarketPrice
package com.likelion.picklbe.domain.marketprice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "MarketPrice")
public class MarketPrice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_name", nullable = false, length = 200)
  private String productName;

  @Column(name = "unit", nullable = false, length = 100)
  private String unit;

  @Column(name = "market_price", nullable = false)
  private double marketPrice;

  @Column(name = "super_market_price", nullable = false)
  private double superMarketPrice;
}
