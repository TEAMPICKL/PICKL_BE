package com.likelion.picklbe.domain.quiz.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "quiz_daily_attempt_tokens",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_quiz_tokens_user_date",
            columnNames = {"user_id", "token_date"}))
@Getter
@Setter
@NoArgsConstructor
public class QuizDailyAttemptToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "token_date", nullable = false)
  private LocalDate tokenDate;

  @Column(nullable = false)
  private int tokens;

  @Column(name = "ad_grants", nullable = false)
  private int adGrants; // 오늘 광고 보상 지급 횟수

  public QuizDailyAttemptToken(Long userId, LocalDate today) {
    this.userId = userId;
    this.tokenDate = today;
    this.tokens = 0;
    this.adGrants = 0;
  }

  /** 날짜가 바뀌었으면 일일 상태 리셋 */
  public void rolloverIfNewDay(LocalDate today) {
    if (tokenDate == null || tokenDate.isBefore(today)) {
      tokenDate = today;
      adGrants = 0;
      // tokens를 0으로 리셋할지 유지할지는 정책에 따라.
      // 보통 "추가 시도권"만 관리한다면 유지해도 되고, 일일 시도권까지 포함한다면 리셋.
      // 여기서는 '추가 시도권'만 관리한다고 보고 유지.
    }
  }

  public void grantFromAdOnce() {
    tokens += 1;
    adGrants += 1;
  }
}
