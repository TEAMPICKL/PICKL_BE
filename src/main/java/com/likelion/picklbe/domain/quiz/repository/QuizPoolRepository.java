package com.likelion.picklbe.domain.quiz.repository;

import java.time.LocalDate;
import java.util.List;

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
}