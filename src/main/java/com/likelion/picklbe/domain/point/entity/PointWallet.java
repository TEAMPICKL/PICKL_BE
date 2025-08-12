package com.likelion.picklbe.domain.point.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "point_wallet")
@Getter
@Setter
public class PointWallet {

  @Id private Long userId;

  @Column(nullable = false)
  private Long balance = 0L;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  void tick() {
    this.updatedAt = LocalDateTime.now();
  }
}
