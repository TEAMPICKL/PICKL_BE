package com.likelion.picklbe.domain.dailypricechange.entity;

import java.time.LocalDate;

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
    name = "kamis_category_summary",
    indexes = {@Index(name = "idx_cat_date", columnList = "priceDate")},
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_cat_unique",
          columnNames = {"priceDate", "productClsName", "categoryCode"})
    })
public class KamisCategorySummary {

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

  @Column(name = "avgLatestPrice", nullable = false)
  private double avgLatestPrice;

  @Column(name = "avgOneDayAgoPrice", nullable = false)
  private double avgOneDayAgoPrice;

  @Column(name = "priceDiff", nullable = false)
  private double priceDiff;

  @Column(name = "priceDiffRate", nullable = false)
  private double priceDiffRate;
}
