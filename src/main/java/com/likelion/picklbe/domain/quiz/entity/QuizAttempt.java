package com.likelion.picklbe.domain.quiz.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "quiz_attempt",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_quiz_attempt_user_date_no",
            columnNames = {"user_id", "quiz_date", "attempt_no"}))
@Getter
@Setter
public class QuizAttempt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "quiz_date", nullable = false)
  private LocalDate quizDate;

  @Column(name = "attempt_no", nullable = false)
  private Integer attemptNo = 1;

  @Column(name = "quiz_pool_id")
  private Long quizPoolId;

  @Column(nullable = false)
  private Boolean answer; // 사용자 제출 O/X

  @Column(nullable = false)
  private Boolean isCorrect;

  @Column(nullable = false)
  private Integer pointsAwarded = 0;

  @Column(nullable = false)
  private LocalDateTime answeredAt;

  @PrePersist
  void pre() {
    this.answeredAt = LocalDateTime.now();
  }
}
