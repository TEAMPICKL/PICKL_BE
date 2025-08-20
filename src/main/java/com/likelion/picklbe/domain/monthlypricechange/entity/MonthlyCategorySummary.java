package com.likelion.picklbe.domain.monthlypricechange.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "monthly_category_summary",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_mcs_cls_cat_ym",
            columnNames = {"product_cls_name", "category_code", "yyyymm"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyCategorySummary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_cls_name")
  private String productClsName; // 소매 | 도매

  @Column(name = "category_code")
  private String categoryCode; // 100~600

  @Column(name = "category_name")
  private String categoryName;

  @Column(name = "yyyymm", length = 6)
  private String yyyymm;

  @Column(name = "avg_max_price")
  private Double avgMaxPrice;
}
