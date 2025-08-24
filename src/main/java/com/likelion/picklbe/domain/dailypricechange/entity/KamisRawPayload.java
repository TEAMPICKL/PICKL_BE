package com.likelion.picklbe.domain.dailypricechange.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
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
    name = "kamis_raw_payload",
    indexes = {@Index(name = "idx_raw_date", columnList = "priceDate")},
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_raw_date_hash",
          columnNames = {"priceDate", "contentHash"})
    })
public class KamisRawPayload {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "priceDate", nullable = false)
  private LocalDate priceDate;

  @Column(name = "fetchedAt", nullable = false)
  private LocalDateTime fetchedAt;

  @Column(name = "contentHash", nullable = false, length = 64)
  private String contentHash;

  @Lob
  @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
  private String payload;
}
