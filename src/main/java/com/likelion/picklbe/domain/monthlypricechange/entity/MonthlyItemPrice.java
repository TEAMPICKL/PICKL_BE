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
    name = "monthly_item_price",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_mip_cls_cat_pno_ym",
            columnNames = {"product_cls_name", "category_code", "product_no", "yyyymm"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyItemPrice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_no")
  private String productNo;

  @Column(name = "product_name")
  private String productName;

  @Column(name = "product_cls_name") // 소매 | 도매
  private String productClsName;

  @Column(name = "category_code")
  private String categoryCode;

  @Column(name = "category_name")
  private String categoryName;

  @Column(name = "yyyymm", length = 6)
  private String yyyymm;

  @Column(name = "price_max")
  private Double priceMax;
}
