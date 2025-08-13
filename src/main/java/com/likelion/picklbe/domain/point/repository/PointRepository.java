package com.likelion.picklbe.domain.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.likelion.picklbe.domain.point.entity.PointTransaction;

public interface PointRepository extends JpaRepository<PointTransaction, Long> {

  @Query("select coalesce(sum(p.amount),0) from PointTransaction p where p.user.id = :userId")
  long sumByUserId(Long userId);
}
