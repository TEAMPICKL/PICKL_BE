package com.likelion.picklbe.domain.mart.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.likelion.picklbe.domain.mart.entity.Place;
import com.likelion.picklbe.domain.mart.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
    value = "app.place-loader.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class PlaceCsvLoader {

  private final PlaceRepository repo;
  private final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);

  // 정제 결과 파일을 기본값으로
  @Value("${app.place-loader.input:file:build/marts_normalized.csv}")
  private Resource inputCsv;

  /** Runner가 호출하는 공개 메서드 */
  public void run() throws Exception {
    if (repo.count() > 0) {
      log.info("[PlaceCsvLoader] place table already filled. skip.");
      return;
    }
    if (!inputCsv.exists()) {
      log.warn("[PlaceCsvLoader] input CSV not found: {}", inputCsv);
      return;
    }

    int inserted = 0, skipped = 0, bad = 0;
    try (Reader reader = new InputStreamReader(inputCsv.getInputStream(), StandardCharsets.UTF_8);
        CSVParser parser =
            CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withTrim()
                .withIgnoreEmptyLines()
                .parse(reader)) {

      // 기대 헤더: name,brand,category,address,lat,lng,phone
      for (CSVRecord r : parser) {
        try {
          String name = val(r, "name");
          String brand = emptyToNull(val(r, "brand"));
          String categoryStr = val(r, "category");
          String address = emptyToNull(val(r, "address"));
          String latStr = val(r, "lat");
          String lngStr = val(r, "lng");
          String phone = emptyToNull(val(r, "phone")); // CSV에서 읽되 엔티티에는 저장하지 않음

          // 헤더 문자열이 데이터로 섞여 들어온 경우 방어
          if (equalsAnyIgnoreCase(address, "address")
              || equalsAnyIgnoreCase(name, "name")
              || equalsAnyIgnoreCase(latStr, "lat")
              || equalsAnyIgnoreCase(lngStr, "lng")
              || equalsAnyIgnoreCase(categoryStr, "category")
              || equalsAnyIgnoreCase(brand, "brand")
              || equalsAnyIgnoreCase(phone, "phone")) {
            skipped++;
            log.warn("[PlaceCsvLoader] skip (header-like row): row #{}", r.getRecordNumber());
            continue;
          }

          Place.Category category = parseCategory(categoryStr);
          if (category == null) {
            skipped++;
            log.warn(
                "[PlaceCsvLoader] skip (bad category): {} (row #{})",
                categoryStr,
                r.getRecordNumber());
            continue;
          }

          double lat = Double.parseDouble(latStr);
          double lng = Double.parseDouble(lngStr);
          Point pt = gf.createPoint(new Coordinate(lng, lat)); // (lng,lat)!

          repo.save(
              Place.builder()
                  .name(name)
                  .brand(brand)
                  .category(category)
                  .address(address)
                  .lat(lat)
                  .lng(lng)
                  .location(pt) // POINT(SRID 4326)
                  // .phone(phone) // 엔티티에 필드 없으면 저장하지 않음
                  .build());

          inserted++;
        } catch (Exception e) {
          bad++;
          log.warn("[PlaceCsvLoader] row {} failed: {}", r.getRecordNumber(), e.toString());
        }
      }
    }
    log.info(
        "[PlaceCsvLoader] done. inserted={}, skipped={}, bad={}, file={}",
        inserted,
        skipped,
        bad,
        inputCsv);
  }

  private String val(CSVRecord r, String key) {
    String v = r.isMapped(key) ? r.get(key) : null;
    return v == null ? "" : v.trim();
  }

  private String emptyToNull(String s) {
    return (s == null || s.isBlank()) ? null : s;
  }

  private boolean equalsAnyIgnoreCase(String s, String... tokens) {
    if (s == null) {
      return false;
    }
    for (String t : tokens) {
      if (s.equalsIgnoreCase(t)) {
        return true;
      }
    }
    return false;
  }

  private Place.Category parseCategory(String raw) {
    if (raw == null) {
      return null;
    }
    String s = raw.trim().replaceAll("^\"|\"$", "").toUpperCase(); // 따옴표/대소문자 방어
    return switch (s) {
      case "HYPERMARKET" -> Place.Category.HYPERMARKET;
      case "SUPERMARKET" -> Place.Category.SUPERMARKET;
      default -> null;
    };
  }
}
