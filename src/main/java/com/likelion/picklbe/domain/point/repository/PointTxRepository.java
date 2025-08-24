package com.likelion.picklbe.domain.point.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.point.entity.PointTx;

public interface PointTxRepository extends JpaRepository<PointTx, Long> {

  Page<PointTx> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  boolean existsByUserIdAndReasonAndRefId(Long userId, String reason, Long refId);
}
