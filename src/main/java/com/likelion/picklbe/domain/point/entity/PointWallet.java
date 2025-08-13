package com.likelion.picklbe.domain.point.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "point_wallet")
@Getter
@Setter
public class PointWallet {

  /** user_id를 PK로 사용 (users.id와 공유 PK가 아니라면 단독 PK로 사용) */
  @Id
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "balance", nullable = false)
  private Long balance = 0L;

  /** 낙관적 락으로 동시 적립 충돌 방지 */
  @Version
  @Column(name = "version", nullable = false)
  private Long version; // Hibernate가 관리(INSERT 시 0으로 설정). 수동 set 금지.

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /** 편의 메서드: 음수로 떨어지지 않게 보호 */
  public void add(long amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("amount must be >= 0");
    }
    this.balance += amount;
  }

  public void subtract(long amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("amount must be >= 0");
    }
    long next = this.balance - amount;
    if (next < 0) {
      throw new IllegalStateException("잔액 부족");
    }
    this.balance = next;
  }

  @PrePersist
  void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
    // 굳이 초기화할 필요는 없지만, 명시적으로 0L로 두고 싶다면 아래 라인 유지
    if (this.version == null) {
      this.version = 0L;
    }
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
