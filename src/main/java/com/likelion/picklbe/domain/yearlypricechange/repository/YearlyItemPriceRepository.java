package com.likelion.picklbe.domain.yearlypricechange.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.likelion.picklbe.domain.yearlypricechange.entity.YearlyItemPrice;

public interface YearlyItemPriceRepository extends JpaRepository<YearlyItemPrice, Long> {

  List<YearlyItemPrice> findByProductNoOrderByYyyyAsc(String productNo);

  void deleteByProductNoInAndYyyyIn(Collection<String> productNos, Collection<String> years);

  // ▼ 전체 productNo 목록 (중복 제거)
  @Query("select distinct y.productNo from YearlyItemPrice y where y.productNo is not null")
  List<String> findDistinctProductNos();

  // ▼ 여러 품목을 한 번에, 정렬된 상태로
  List<YearlyItemPrice> findByProductNoInOrderByProductNoAscYyyyAsc(Collection<String> productNos);

  // ▼ 시장+카테고리로 필터해 한 번에 가져오기 (집계 전용)
  List<YearlyItemPrice> findByProductClsNameAndCategoryCodeOrderByYyyyAsc(
      String productClsName, String categoryCode);
}
