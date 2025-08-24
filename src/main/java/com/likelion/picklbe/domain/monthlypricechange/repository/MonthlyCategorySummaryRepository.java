package com.likelion.picklbe.domain.monthlypricechange.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.monthlypricechange.entity.MonthlyCategorySummary;

public interface MonthlyCategorySummaryRepository
    extends JpaRepository<MonthlyCategorySummary, Long> {

  List<MonthlyCategorySummary> findByProductClsNameAndCategoryCodeOrderByYyyymmAsc(
      String market, String categoryCode);

  void deleteByProductClsNameInAndCategoryCodeInAndYyyymmIn(
      Collection<String> markets, Collection<String> categories, Collection<String> yyyymms);

  List<MonthlyCategorySummary> findAllByOrderByProductClsNameAscCategoryCodeAscYyyymmAsc();

  List<MonthlyCategorySummary> findByProductClsNameOrderByCategoryCodeAscYyyymmAsc(String market);
}
