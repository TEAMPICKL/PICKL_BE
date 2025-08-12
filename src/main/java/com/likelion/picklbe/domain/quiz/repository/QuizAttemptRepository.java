package com.likelion.picklbe.domain.quiz.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.quiz.entity.QuizAttempt;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

  boolean existsByUserIdAndQuizDate(Long userId, LocalDate date);

  List<QuizAttempt> findByUserIdAndQuizDate(Long userId, LocalDate date);
}
