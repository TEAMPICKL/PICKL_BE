package com.likelion.picklbe.domain.quiz.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.likelion.picklbe.domain.ingredient.entity.Ingredient;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "daily_quiz")
@Getter
@Setter
public class DailyQuiz {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private LocalDate quizDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "quiz_pool_id", nullable = false)
  private QuizPool quizPool;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient_id", nullable = false)
  private Ingredient ingredient;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String statement;

  @Column(nullable = false)
  private Boolean answer;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void pre() {
    this.createdAt = LocalDateTime.now();
  }
}
