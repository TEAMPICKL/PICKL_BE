package com.likelion.picklbe.domain.dailypricechange.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.dailypricechange.entity.KamisRawPayload;

public interface KamisRawPayloadRepository extends JpaRepository<KamisRawPayload, Long> {

  List<KamisRawPayload> findByPriceDateOrderByFetchedAtDesc(LocalDate priceDate);

  Optional<KamisRawPayload> findFirstByPriceDateOrderByFetchedAtDesc(LocalDate priceDate);
}
