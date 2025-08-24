package com.likelion.picklbe.infra.geo;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class VWorldGeocoder {

  // RestTemplate를 내부에서 생성(타임아웃 설정 포함)
  private final RestTemplate restTemplate = createRestTemplate();

  @Value("${vworld.key}")
  private String apiKey;

  /**
   * 주소 → (lat,lng) 지오코딩. - vworld 도로명(type=road) 우선 시도 - 실패 시 지번(type=parcel)로 재시도 - 정상 결과 없거나 예외면
   * Optional.empty()
   */
  public Optional<Coord> geocode(String address) {
    if (apiKey == null || apiKey.isBlank()) {
      log.warn("[VWorldGeocoder] API key missing; skip address={}", address);
      return Optional.empty();
    }
    if (address == null || address.isBlank()) {
      return Optional.empty();
    }

    try {
      // 1차: 도로명
      Optional<Coord> road = requestOnce(address, "road");
      if (road.isPresent()) {
        return road;
      }

      // 2차: 지번
      Optional<Coord> parcel = requestOnce(address, "parcel");
      return parcel;
    } catch (Exception e) {
      log.warn("[VWorldGeocoder] fail address={}", address, e);
      return Optional.empty();
    }
  }

  /** VWorld API를 한 번 호출하여 좌표를 파싱. 성공 시 Optional.of(Coord), 실패/무결성 문제 시 Optional.empty() */
  private Optional<Coord> requestOnce(String address, String type) {
    try {
      URI uri =
          UriComponentsBuilder.fromHttpUrl("https://api.vworld.kr/req/address")
              .queryParam("service", "address")
              .queryParam("request", "getcoord")
              .queryParam("crs", "epsg:4326")
              .queryParam("format", "json")
              .queryParam("type", type) // "road" 또는 "parcel"
              .queryParam("key", apiKey)
              .queryParam("address", address) // 자동 인코딩
              .build(true) // 인코딩 허용
              .toUri();

      @SuppressWarnings("unchecked")
      Map<String, Object> resp = restTemplate.getForObject(uri, Map.class);
      if (resp == null) {
        log.debug("[VWorldGeocoder] empty response type={} address={}", type, address);
        return Optional.empty();
      }

      @SuppressWarnings("unchecked")
      Map<String, Object> response = (Map<String, Object>) resp.get("response");
      if (response == null || !"OK".equals(response.get("status"))) {
        log.debug(
            "[VWorldGeocoder] non-OK status type={} address={} status={}",
            type,
            address,
            response == null ? null : response.get("status"));
        return Optional.empty();
      }

      // vworld는 결과 없으면 result 자체가 없거나 빈 배열일 수 있음
      Object resultObj = response.get("result");
      if (resultObj == null) {
        log.debug("[VWorldGeocoder] no result field type={} address={}", type, address);
        return Optional.empty();
      }

      // result가 배열 또는 단일 객체일 수 있으므로 유연하게 처리
      Map<String, Object> firstResult;
      if (resultObj instanceof List<?> list) {
        if (list.isEmpty()) {
          log.debug("[VWorldGeocoder] empty result list type={} address={}", type, address);
          return Optional.empty();
        }
        Object first = list.get(0);
        if (!(first instanceof Map<?, ?> m)) {
          return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> casted = (Map<String, Object>) m;
        firstResult = casted;
      } else if (resultObj instanceof Map<?, ?> m) {
        @SuppressWarnings("unchecked")
        Map<String, Object> casted = (Map<String, Object>) m;
        firstResult = casted;
      } else {
        return Optional.empty();
      }

      @SuppressWarnings("unchecked")
      Map<String, Object> point = (Map<String, Object>) firstResult.get("point");
      if (point == null) {
        log.debug("[VWorldGeocoder] missing point type={} address={}", type, address);
        return Optional.empty();
      }

      Object xObj = point.get("x");
      Object yObj = point.get("y");
      if (xObj == null || yObj == null) {
        log.debug("[VWorldGeocoder] missing x/y type={} address={}", type, address);
        return Optional.empty();
      }

      double lng = Double.parseDouble(String.valueOf(xObj));
      double lat = Double.parseDouble(String.valueOf(yObj));
      return Optional.of(new Coord(lat, lng));
    } catch (Exception e) {
      // 여기서는 재시도하지 않고 상위에서 parcel 시도/종료
      log.debug(
          "[VWorldGeocoder] requestOnce error type={} address={} err={}",
          type,
          address,
          e.toString());
      return Optional.empty();
    }
  }

  private static RestTemplate createRestTemplate() {
    // 간단한 타임아웃 설정(연결/읽기 각 5초)
    SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
    int timeoutMs = (int) Duration.ofSeconds(5).toMillis();
    rf.setConnectTimeout(timeoutMs);
    rf.setReadTimeout(timeoutMs);
    return new RestTemplate(rf);
  }

  /** lat, lng (EPSG:4326) */
  public record Coord(double lat, double lng) {}
}
