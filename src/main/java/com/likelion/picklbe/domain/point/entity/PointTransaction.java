package com.likelion.picklbe.domain.point.entity;

import jakarta.persistence.*;

import com.likelion.picklbe.domain.user.entity.User;

import lombok.*;

@Entity
@Table(
    name = "point_transactions",
    indexes = {@Index(name = "idx_pt_user", columnList = "user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private long amount; // 적립은 +, 사용은 - 로 저장하면 합계가 총 포인트

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Reason reason; // DAILY_BONUS, PURCHASE, EVENT, ...

  public enum Reason {
    DAILY_BONUS,
    PURCHASE,
    EVENT
  }
}
