package com.likelion.picklbe.domain.quiz.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.likelion.picklbe.domain.quiz.entity.QuizPool;

public interface QuizPoolRepository extends JpaRepository<QuizPool, Long> {

  @Query(
      """
          SELECT q FROM QuizPool q
          WHERE q.isActive = true
            AND (q.lastUsedDate IS NULL OR q.lastUsedDate <= :threshold)
          ORDER BY function('RAND')
          """)
  List<QuizPool> findPickableRandom(@Param("threshold") LocalDate threshold, Pageable pageable);

  // 전체에서 랜덤 (폴백)
  @Query("select qp from QuizPool qp order by function('rand')")
  List<QuizPool> findRandom(Pageable pageable);

  // 아이콘이 있는 랜덤 퀴즈 뽑기
  @Query(
      value =
          """
              SELECT qp.id AS quizId, i.name AS ingredientName, qp.statement AS statement, i.icon_url AS icon
              FROM quiz_pool qp
              JOIN ingredient i ON i.id = qp.ingredient_id
              WHERE qp.is_active = true
                AND i.icon_url IS NOT NULL
                AND i.name NOT IN ('미역','대파','다시마','곶감')
              ORDER BY RAND()
              LIMIT 1
              """,
      nativeQuery = true)
  Optional<Map<String, Object>> pickRandomWithIcon();

  // 특정 ID들을 제외하고 랜덤으로 퀴즈 뽑기
  @Query(
      """
          SELECT q FROM QuizPool q
          WHERE q.isActive = true
            AND (:excludeSize = 0 OR q.id NOT IN :exclude)
          ORDER BY function('RAND')
          """)
  List<QuizPool> pickOneExcluding(
      @Param("exclude") List<Long> exclude,
      @Param("excludeSize") int excludeSize,
      Pageable pageable);
}
