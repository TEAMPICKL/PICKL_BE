package com.likelion.picklbe.domain.quiz.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.likelion.picklbe.domain.ingredient.entity.Ingredient;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "quiz_pool")
@Getter
@Setter
public class QuizPool {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient_id", nullable = false)
  private Ingredient ingredient;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String statement;

  @Column(nullable = false)
  private Boolean answer; // true=O, false=X

  @Column(nullable = false)
  private Boolean isActive = true;

  private LocalDate lastUsedDate;
}
