package com.likelion.picklbe.domain.history.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.history.entity.PickleHistory;

public interface PickleHistoryRepository extends JpaRepository<PickleHistory, Long> {

  int countByUserId(Long userId);
}
