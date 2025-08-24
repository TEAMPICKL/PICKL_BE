package com.likelion.picklbe.domain.monthlypricechange.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.likelion.picklbe.domain.monthlypricechange.entity.KamisMonthlyRawPayload;

public interface KamisMonthlyRawPayloadRepository
    extends JpaRepository<KamisMonthlyRawPayload, Long> {

  Optional<KamisMonthlyRawPayload>
      findFirstByYearAndCategoryCodeAndItemCodeAndKindCodeAndGradeRankAndCountyCodeOrderByFetchedAtDesc(
          int year,
          String categoryCode,
          String itemCode,
          String kindCode,
          String gradeRank,
          String countyCode);

  // ✅ year만(또는 일부 필터만) 받아서 전부 가져온 뒤, 서비스에서 조합별 최신건으로 dedupe
  @Query(
      """
          select r
            from KamisMonthlyRawPayload r
           where r.year = :year
             and (:categoryCode is null or r.categoryCode = :categoryCode)
             and (:itemCode    is null or r.itemCode    = :itemCode)
             and (:kindCode    is null or r.kindCode    = :kindCode)
             and (:gradeRank   is null or r.gradeRank   = :gradeRank)
             and (:countyCode  is null or r.countyCode  = :countyCode)
           order by r.categoryCode asc, r.itemCode asc, r.kindCode asc, r.fetchedAt desc
          """)
  List<KamisMonthlyRawPayload> findAllByYearAndOptionalFilters(
      @Param("year") int year,
      @Param("categoryCode") String categoryCode,
      @Param("itemCode") String itemCode,
      @Param("kindCode") String kindCode,
      @Param("gradeRank") String gradeRank,
      @Param("countyCode") String countyCode);
}
