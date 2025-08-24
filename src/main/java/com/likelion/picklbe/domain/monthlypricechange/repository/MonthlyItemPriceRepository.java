package com.likelion.picklbe.domain.monthlypricechange.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.likelion.picklbe.domain.monthlypricechange.entity.MonthlyItemPrice;

public interface MonthlyItemPriceRepository extends JpaRepository<MonthlyItemPrice, Long> {

  List<MonthlyItemPrice> findByProductNoOrderByYyyymmAsc(String productNo);

  @Query(
      """
      select distinct mip.productNo from MonthlyItemPrice mip
      order by mip.productNo asc
      """)
  List<String> findDistinctProductNos();

  List<MonthlyItemPrice> findByProductNoInOrderByProductNoAscYyyymmAsc(
      Collection<String> productNos);

  void deleteByProductNoInAndYyyymmIn(Collection<String> productNos, Collection<String> yyyymms);

  List<MonthlyItemPrice> findByProductClsNameAndCategoryCodeOrderByYyyymmAsc(
      String cls, String cat);
}
