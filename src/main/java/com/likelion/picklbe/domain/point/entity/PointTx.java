package com.likelion.picklbe.domain.point.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "point_tx",
    uniqueConstraints = {
      // 사용자 + 사유 + 참조ID 한 번만 적립(퀴즈 중복 방지 등)
      @UniqueConstraint(
          name = "uk_user_reason_ref",
          columnNames = {"user_id", "reason", "ref_id"}),
      // 선택: 멱등키 사용 시 중복 방지 (컬럼 추가한 경우만)
      // @UniqueConstraint(name = "uk_idempotency", columnNames = {"idempotency"})
    })
@Getter
@Setter
public class PointTx {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long amount;

  /** 사유/소스. 예) "QUIZ_DAILY", "PROMO", "AD_REWARD" 등 (enum 대신 문자열로 운영 중이므로 그대로 유지) */
  @Column(nullable = false, length = 40)
  private String reason;

  /** 관련 엔터티 id (오늘의 퀴즈 id 등). 없으면 null */
  @Column(name = "ref_id")
  private Long refId;

  /** 선택: 멱등키를 쓰고 싶으면 컬럼 추가하고 유니크 켜기 */
  // @Column(length = 64)
  // private String idempotency;

  @Column(length = 255)
  private String description;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void prePersist() {
    this.createdAt = LocalDateTime.now();
  }
}
