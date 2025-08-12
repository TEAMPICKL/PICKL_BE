package com.likelion.picklbe.domain.quiz.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.quiz.entity.DailyQuiz;

public interface DailyQuizRepository extends JpaRepository<DailyQuiz, Long> {

  Optional<DailyQuiz> findByQuizDate(LocalDate date);

  boolean existsByQuizDate(LocalDate date);
}
