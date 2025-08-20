// src/main/java/com/likelion/picklbe/domain/yearlypricechange/entity/YearlyItemPrice.java
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
    name = "yearly_item_price",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_yip_cls_cat_pno_year",
            columnNames = {"product_cls_name", "category_code", "product_no", "yyyy"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlyItemPrice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 메타 (Daily 테이블에서 끌어옴)
  @Column(name = "product_no")
  private String productNo;

  @Column(name = "product_name")
  private String productName;

  @Column(name = "product_cls_name") // "소매" | "도매"
  private String productClsName;

  @Column(name = "category_code")
  private String categoryCode;

  @Column(name = "category_name")
  private String categoryName;

  // 시계열 키
  @Column(name = "yyyy", length = 4)
  private String yyyy;

  // 값: yearly API의 max를 가격으로 사용
  @Column(name = "price_max")
  private Double priceMax;
}
