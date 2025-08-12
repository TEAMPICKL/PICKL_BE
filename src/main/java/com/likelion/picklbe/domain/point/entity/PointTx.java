package com.likelion.picklbe.domain.point.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "point_tx")
@Getter
@Setter
public class PointTx {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long amount;

  @Column(nullable = false, length = 40)
  private String reason; // QUIZ_DAILY

  private Long refId;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void pre() {
    this.createdAt = LocalDateTime.now();
  }
}
