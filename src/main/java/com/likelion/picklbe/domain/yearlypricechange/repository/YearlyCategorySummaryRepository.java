package com.likelion.picklbe.domain.yearlypricechange.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.yearlypricechange.entity.YearlyCategorySummary;

public interface YearlyCategorySummaryRepository
    extends JpaRepository<YearlyCategorySummary, Long> {

  List<YearlyCategorySummary> findByProductClsNameAndCategoryCodeOrderByYyyyAsc(
      String market, String categoryCode);

  void deleteByProductClsNameInAndCategoryCodeInAndYyyyIn(
      Collection<String> markets, Collection<String> categories, Collection<String> years);

  // ▼ 전체(시장/카테고리/연도)를 정렬해 한 번에
  List<YearlyCategorySummary> findAllByOrderByProductClsNameAscCategoryCodeAscYyyyAsc();

  // ▼ 시장만 제한해서(카테고리 전체)
  List<YearlyCategorySummary> findByProductClsNameOrderByCategoryCodeAscYyyyAsc(String market);
}
