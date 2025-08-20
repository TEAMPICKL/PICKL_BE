package com.likelion.picklbe.domain.quiz.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_daily_question")
@Getter
@Setter
public class UserDailyQuestion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;
  private LocalDate quizDate;
  private Integer attemptNo;
  private Long quizPoolId;
}
