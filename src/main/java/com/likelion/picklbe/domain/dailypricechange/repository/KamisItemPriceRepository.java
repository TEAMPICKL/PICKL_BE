package com.likelion.picklbe.domain.dailypricechange.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.likelion.picklbe.domain.dailypricechange.entity.KamisItemPrice;

public interface KamisItemPriceRepository extends JpaRepository<KamisItemPrice, Long> {

  // ---- 기본 조회/삭제
  void deleteByPriceDate(LocalDate priceDate);

  List<KamisItemPrice> findByPriceDate(LocalDate priceDate);

  List<KamisItemPrice> findByPriceDateAndProductClsName(LocalDate priceDate, String productClsName);

  List<KamisItemPrice> findByPriceDateAndProductNameContainingIgnoreCase(
      LocalDate priceDate, String productName);

  List<KamisItemPrice> findByPriceDateAndProductClsNameAndProductNameContainingIgnoreCase(
      LocalDate date, String cls, String name);

  @Query("select max(k.priceDate) from KamisItemPrice k")
  LocalDate findLatestPriceDate();

  List<KamisItemPrice> findByPriceDateAndProductNo(LocalDate date, String productNo);

  List<KamisItemPrice> findByPriceDateAndProductClsNameAndProductNo(
      LocalDate date, String cls, String productNo);

  // ---- 이미지 URL 누락건
  List<KamisItemPrice> findByImageUrlIsNullOrderByIdAsc(Pageable pageable);

  List<KamisItemPrice> findByPriceDateAndImageUrlIsNullOrderByIdAsc(
      LocalDate date, Pageable pageable);

  List<KamisItemPrice> findByPriceDateAndProductClsNameAndImageUrlIsNullOrderByIdAsc(
      LocalDate date, String cls, Pageable pageable);

  List<KamisItemPrice> findAllByOrderByIdAsc(Pageable pageable);

  List<KamisItemPrice> findByPriceDateOrderByIdAsc(LocalDate date, Pageable pageable);

  List<KamisItemPrice> findByPriceDateAndProductClsNameOrderByIdAsc(
      LocalDate date, String cls, Pageable pageable);

  // ---- 배치용 productNo 목록 (페이징)
  @Query(
      value =
          """
                select distinct k.productNo
                  from KamisItemPrice k
                 where (:priceDate is null or k.priceDate = :priceDate)
                   and (:market   is null or k.productClsName = :market)
                   and k.productNo is not null
                 order by k.productNo
              """,
      countQuery =
          """
                select count(distinct k.productNo)
                  from KamisItemPrice k
                 where (:priceDate is null or k.priceDate = :priceDate)
                   and (:market   is null or k.productClsName = :market)
                   and k.productNo is not null
              """)
  Page<String> findDistinctProductNo(
      @Param("priceDate") LocalDate priceDate, @Param("market") String market, Pageable pageable);

  // ---- (카테고리, 아이템, 품종) 조합 조회
  @Query(
      """
            select distinct i.categoryCode as categoryCode,
                            i.itemCode     as itemCode,
                            i.kindCode     as kindCode
              from KamisItemPrice i
             where i.priceDate = :date
               and i.itemCode is not null
               and i.kindCode is not null
               and (:categoryCode  is null or i.categoryCode  = :categoryCode)
               and (:productClsCode is null or i.productClsCode = :productClsCode)
             order by i.categoryCode asc, i.itemCode asc
          """)
  List<ItemKindCatView> findDistinctItemKindPairs(
      @Param("date") LocalDate date,
      @Param("categoryCode") String categoryCode,
      @Param("productClsCode") String productClsCode);

  // 이미지 없는 것들 중 startId 이상만
  List<KamisItemPrice> findByPriceDateAndIdGreaterThanEqualAndImageUrlIsNullOrderByIdAsc(
      LocalDate date, Long startId, Pageable pageable);

  List<KamisItemPrice>
      findByPriceDateAndProductClsNameAndIdGreaterThanEqualAndImageUrlIsNullOrderByIdAsc(
          LocalDate date, String clsName, Long startId, Pageable pageable);

  // refresh 모드일 때도 대비
  List<KamisItemPrice> findByPriceDateAndIdGreaterThanEqualOrderByIdAsc(
      LocalDate date, Long startId, Pageable pageable);

  List<KamisItemPrice> findByPriceDateAndProductClsNameAndIdGreaterThanEqualOrderByIdAsc(
      LocalDate date, String clsName, Long startId, Pageable pageable);
}
