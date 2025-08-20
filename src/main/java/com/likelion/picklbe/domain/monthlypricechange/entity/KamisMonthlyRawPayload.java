package com.likelion.picklbe.domain.monthlypricechange.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kamis_monthly_raw_payload")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KamisMonthlyRawPayload {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private int year;
  private String categoryCode;
  private String itemCode;
  private String kindCode;
  private String gradeRank;
  private String countyCode;

  private String productClsCode; // "01"/"02"
  private String caption;

  private LocalDateTime fetchedAt;
  private String contentHash;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String payload;
}
