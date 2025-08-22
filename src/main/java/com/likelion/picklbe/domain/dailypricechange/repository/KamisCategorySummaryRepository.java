package com.likelion.picklbe.domain.dailypricechange.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.dailypricechange.entity.KamisCategorySummary;

public interface KamisCategorySummaryRepository extends JpaRepository<KamisCategorySummary, Long> {

  void deleteByPriceDate(LocalDate priceDate);

  List<KamisCategorySummary> findByPriceDate(LocalDate priceDate);

  List<KamisCategorySummary> findByPriceDateAndProductClsName(
      LocalDate priceDate, String productClsName);

  List<KamisCategorySummary> findByPriceDateAndCategoryCode(
      LocalDate priceDate, String categoryCode);

  List<KamisCategorySummary> findByPriceDateAndProductClsNameAndCategoryCode(
      LocalDate priceDate, String productClsName, String categoryCode);
}
