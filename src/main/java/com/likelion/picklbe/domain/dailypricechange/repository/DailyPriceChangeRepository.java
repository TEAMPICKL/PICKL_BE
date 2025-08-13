package com.likelion.picklbe.domain.dailypricechange.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.dailypricechange.entity.DailyPriceChange;

public interface DailyPriceChangeRepository extends JpaRepository<DailyPriceChange, Long> {}
