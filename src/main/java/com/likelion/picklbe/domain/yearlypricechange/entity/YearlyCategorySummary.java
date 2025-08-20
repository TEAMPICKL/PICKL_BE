package com.likelion.picklbe.domain.yearlypricechange.entity;

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
    name = "yearly_category_summary",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_ycs_cls_cat_year",
            columnNames = {"product_cls_name", "category_code", "yyyy"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlyCategorySummary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_cls_name")
  private String productClsName; // 소매/도매

  @Column(name = "category_code")
  private String categoryCode; // 100~600

  @Column(name = "category_name")
  private String categoryName;

  @Column(name = "yyyy", length = 4)
  private String yyyy;

  // 해당 연도, 해당 카테고리(×시장)에서 품목들의 max 평균
  @Column(name = "avg_max_price")
  private Double avgMaxPrice;

  // 참고: 원한다면 표본 개수, 표준편차 같은 부가지표도 추가 가능
}
