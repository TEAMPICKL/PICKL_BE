package com.likelion.picklbe.domain.quiz.repository;

import java.time.LocalDate;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.likelion.picklbe.domain.quiz.entity.QuizDailyAttemptToken;

public interface QuizDailyAttemptTokenRepository
    extends JpaRepository<QuizDailyAttemptToken, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select t from QuizDailyAttemptToken t "
          + "where t.userId = :userId and t.tokenDate = :today")
  Optional<QuizDailyAttemptToken> findByUserIdAndDateForUpdate(
      @Param("userId") Long userId, @Param("today") LocalDate today);
}
