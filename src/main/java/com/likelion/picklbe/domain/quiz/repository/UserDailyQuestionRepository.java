package com.likelion.picklbe.domain.quiz.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.likelion.picklbe.domain.quiz.entity.UserDailyQuestion;

public interface UserDailyQuestionRepository extends JpaRepository<UserDailyQuestion, Long> {

  @Query(
      """
      select u
      from UserDailyQuestion u
      where u.userId = :userId
        and u.quizDate = :quizDate
        and u.attemptNo = :attemptNo
      """)
  Optional<UserDailyQuestion> findOne(
      @Param("userId") Long userId,
      @Param("quizDate") LocalDate quizDate,
      @Param("attemptNo") int attemptNo);

  boolean existsByUserIdAndQuizDateAndAttemptNo(Long userId, LocalDate quizDate, Integer attemptNo);
}
