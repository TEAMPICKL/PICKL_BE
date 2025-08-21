package com.likelion.picklbe.domain.mart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.likelion.picklbe.domain.mart.entity.Place;

public interface PlaceRepository extends JpaRepository<Place, Long> {

  // 뷰포트 바운딩박스 내 마트(대형/슈퍼) 조회 + 중심점 거리순
  @Query(
      value =
          """
      SELECT p.*
      FROM place p
      WHERE p.category IN ('HYPERMARKET','SUPERMARKET')
        AND MBRWithin(p.location, ST_SRID(
            ST_PolygonFromText(CONCAT('POLYGON((',
               :wLng,' ',:sLat,',',
               :eLng,' ',:sLat,',',
               :eLng,' ',:nLat,',',
               :wLng,' ',:nLat,',',
               :wLng,' ',:sLat,'))')), 4326))
      ORDER BY ST_Distance_Sphere(p.location, ST_SRID(POINT(:centerLng,:centerLat),4326))
      LIMIT :limit
      """,
      nativeQuery = true)
  List<Place> findMartsInBounds(
      @Param("wLng") double westLng,
      @Param("sLat") double southLat,
      @Param("eLng") double eastLng,
      @Param("nLat") double northLat,
      @Param("centerLng") double centerLng,
      @Param("centerLat") double centerLat,
      @Param("limit") int limit);

  // 반경 r 미터 내 마트(줌이 높을 때 사용)
  @Query(
      value =
          """
      SELECT p.*
      FROM place p
      WHERE p.category IN ('HYPERMARKET','SUPERMARKET')
        AND ST_Distance_Sphere(p.location, ST_SRID(POINT(:lng,:lat),4326)) <= :radius
      ORDER BY ST_Distance_Sphere(p.location, ST_SRID(POINT(:lng,:lat),4326))
      LIMIT :limit
      """,
      nativeQuery = true)
  List<Place> findMartsNearby(
      @Param("lng") double lng,
      @Param("lat") double lat,
      @Param("radius") int radiusMeters,
      @Param("limit") int limit);
}
