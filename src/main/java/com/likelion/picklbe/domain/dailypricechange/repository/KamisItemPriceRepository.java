package com.likelion.picklbe.domain.dailypricechange.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.likelion.picklbe.domain.dailypricechange.entity.KamisItemPrice;

public interface KamisItemPriceRepository extends JpaRepository<KamisItemPrice, Long> {

  void deleteByPriceDate(LocalDate priceDate);

  List<KamisItemPrice> findByPriceDate(LocalDate priceDate);

  List<KamisItemPrice> findByPriceDateAndProductClsName(LocalDate priceDate, String productClsName);

  // 이름 부분일치
  List<KamisItemPrice> findByPriceDateAndProductNameContainingIgnoreCase(
      LocalDate priceDate, String productName);

  List<KamisItemPrice> findByPriceDateAndProductClsNameAndProductNameContainingIgnoreCase(
      LocalDate priceDate, String productClsName, String productName);

  // 최신 수집일
  @Query("select max(k.priceDate) from KamisItemPrice k")
  LocalDate findLatestPriceDate();

  // productNo로 검색 (옵션)
  List<KamisItemPrice> findByPriceDateAndProductNo(LocalDate date, String productNo);

  List<KamisItemPrice> findByPriceDateAndProductClsNameAndProductNo(
      LocalDate date, String cls, String productNo);
}
