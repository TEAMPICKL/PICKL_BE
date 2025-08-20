package com.likelion.picklbe.domain.dailypricechange.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "kamis_item_price",
    indexes = {
        @Index(name = "idx_item_date", columnList = "priceDate"),
        @Index(name = "idx_item_cat", columnList = "priceDate,categoryCode")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_item_unique",
            columnNames = {"priceDate", "productClsName", "categoryCode", "productNo"})
    })
public class KamisItemPrice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "raw_id")
  private KamisRawPayload raw;

  @Column(name = "priceDate", nullable = false)
  private LocalDate priceDate;

  @Column(name = "productClsName", nullable = false, length = 20) // "소매"/"도매"
  private String productClsName;

  @Column(name = "categoryCode", nullable = false, length = 20)
  private String categoryCode;

  @Column(name = "categoryName", nullable = false, length = 100)
  private String categoryName;

  @Column(name = "product_no", length = 32) // NULL 허용(마이그레이션 수월)
  private String productNo;

  @Column(name = "productName", nullable = false, length = 120)
  private String productName;

  @Column(name = "unit", nullable = false, length = 50)
  private String unit;

  @Column(name = "latestPrice", nullable = false)
  private double latestPrice;

  @Column(name = "oneDayAgoPrice", nullable = false)
  private double oneDayAgoPrice;

  @Column(name = "priceDiff", nullable = false)
  private double priceDiff;

  @Column(name = "priceDiffRate", nullable = false)
  private double priceDiffRate;

  @Column(name = "imageUrl", length = 512)
  private String imageUrl;

  private String productClsCode;
  private String itemCode;
  private String kindCode;
  private String gradeRank;
  private String countyCode;
}
