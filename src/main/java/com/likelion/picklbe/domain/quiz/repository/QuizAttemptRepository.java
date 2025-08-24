package com.likelion.picklbe.domain.quiz.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.likelion.picklbe.domain.quiz.entity.QuizAttempt;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

  // 컨트롤러에서 사용: 오늘 이미 시도했는지 여부
  boolean existsByUserIdAndQuizDate(Long userId, LocalDate date);

  // (다른 사용처 대비) 오늘 시도한 기록 전체 조회
  List<QuizAttempt> findByUserIdAndQuizDate(Long userId, LocalDate date);

  // 새 서비스(QuizDailyService)에서 사용: 오늘 시도 횟수
  int countByUserIdAndQuizDate(Long userId, LocalDate quizDate);

  // 오늘 사용한 quiz_pool_id 목록 (NULL 제외, 시도 순서대로)
  @Query(
      """
      select qa.quizPoolId
      from QuizAttempt qa
      where qa.userId = :userId
        and qa.quizDate = :quizDate
        and qa.quizPoolId is not null
      order by qa.attemptNo asc
      """)
  List<Long> findQuizPoolIdsByUserAndDate(
      @Param("userId") Long userId, @Param("quizDate") LocalDate quizDate);
}
