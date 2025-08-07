package com.likelion.picklbe.domain.averageprice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.averageprice.entity.AveragePrice;

public interface AveragePriceRepository extends JpaRepository<AveragePrice, Long> {}
