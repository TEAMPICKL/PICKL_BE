package com.likelion.picklbe.domain.mart.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.likelion.picklbe.domain.brand.BrandImageResolver;
import com.likelion.picklbe.infra.geo.VWorldGeocoder;
import com.likelion.picklbe.infra.geo.VWorldGeocoder.Coord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
@ConditionalOnProperty(value = "app.geocode.enabled", havingValue = "true", matchIfMissing = false)
@Service
public class MartCsvNormalizer implements CommandLineRunner {

  private final VWorldGeocoder geocoder;
  private final BrandImageResolver brandImageResolver;

  @Value("${app.geocode.input}")
  private Resource inputCsv;

  @Value("${app.geocode.output}")
  private Resource outputCsv;

  @Value("${app.geocode.throttlePerSec:4}")
  private int throttlePerSec;

  @Value("${app.geocode.maxRetries:3}")
  private int maxRetries;

  @Value("${app.geocode.retryBackoffMs:800}")
  private long retryBackoffMs;

  @Value("${app.geocode.charset:UTF-8}")
  private String charset;

  @Value("${app.geocode.columns.name:사업장명}")
  private String colName;

  @Value("${app.geocode.columns.address:도로명전체주소}")
  private String colAddress;

  @Value("${app.geocode.columns.phone:소재지전화}")
  private String colPhone;

  @Value("${app.geocode.columns.category:개방서비스명}")
  private String colCategory;

  @Value("${app.geocode.columns.biztype:업태구분명}")
  private String colBizType;

  @Value("${app.geocode.columns.x:좌표정보x(epsg5174)}")
  private String colX;

  @Value("${app.geocode.columns.y:좌표정보y(epsg5174)}")
  private String colY;

  @Value("${app.geocode.original-crs:EPSG:5174}")
  private String originalCrs;

  private final Map<String, Coord> cache = new ConcurrentHashMap<>();

  @Override
  public void run(String... args) throws Exception {
    log.info(
        "[Normalizer] cols name='{}', addr='{}', phone='{}', categoype='{}', x='{}', y='{}', crs='{}'",
        colName,
        colAddress,
        colPhone,
        colBizType,
        colX,
        colY,
        originalCrs);

    if (!inputCsv.exists()) {
      log.warn("[Normalizer] input CSV not found: {}", inputCsv);
      return;
    }
    File outFile = resolveOutputFile(outputCsv);
    if (outFile.getParentFile() != null) {
      outFile.getParentFile().mkdirs();
    }

    if (outFile.exists() && outFile.length() > 100) {
      log.info(
          "[Normalizer] existing output found ({} bytes). Skip normalization: {}",
          outFile.length(),
          outFile.getAbsolutePath());
      return;
    }

    Path outPath = outFile.toPath();

    CSVFormat fmt =
        CSVFormat.DEFAULT
            .builder()
            // 파일 헤더가 케이스/공백 틀어져도 대응하려고 명시적 헤더 지정 + echo-row 필터링
            .setHeader(
                "번호",
                "개방서비스명",
                "개방서비스아이디",
                "개방자치단체코드",
                "관리번호",
                "인허가일자",
                "인허가취소일자",
                "영업상태구분코드",
                "영업상태명",
                "상세영업상태코드",
                "상세영업상태명",
                "폐업일자",
                "휴업시작일자",
                "휴업종료일자",
                "재개업일자",
                "소재지전화",
                "소재지면적",
                "소재지우편번호",
                "소재지전체주소",
                "도로명전체주소",
                "도로명우편번호",
                "사업장명",
                "최종수정시점",
                "데이터갱신구분",
                "데이터갱신일자",
                "업태구분명",
                "좌표정보x(epsg5174)",
                "좌표정보y(epsg5174)",
                "점포구분명")
            .setSkipHeaderRecord(true) // 출력쪽에만 영향, 입력은 echo-row 가드로 방지
            .setTrim(true)
            .setIgnoreSurroundingSpaces(true)
            .setAllowMissingColumnNames(true)
            .build();

    try (Reader reader =
            new InputStreamReader(inputCsv.getInputStream(), Charset.forName(charset));
        CSVParser parser = new CSVParser(reader, fmt);
        BufferedWriter w = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8);
        CSVPrinter printer =
            new CSVPrinter(
                w,
                CSVFormat.DEFAULT.withHeader(
                    "name", "brand", "category", "address", "lat", "lng", "phone"))) {

      var headerMap = parser.getHeaderMap();
      List<String> headers = parser.getHeaderNames().stream().map(this::clean).toList();
      log.info("[Normalizer] headers={}", headers);

      boolean hasAddr =
          containsHeader(headerMap, colAddress) || containsHeader(headerMap, "소재지전체주소");
      boolean hasX = containsHeader(headerMap, colX);
      boolean hasY = containsHeader(headerMap, colY);
      if (!hasAddr && !(hasX && hasY)) {
        throw new IllegalArgumentException(
            "Need address ["
                + colAddress
                + " or 소재지전체주소] or both coords ["
                + colX
                + ","
                + colY
                + "]");
      }

      require(headerMap, colCategory);
      require(headerMap, colBizType);

      long processed = 0,
          ok = 0,
          skipped = 0,
          failed = 0,
          noAddr = 0,
          badCat = 0,
          noCoord = 0,
          filtered = 0;
      RateLimiter limiter = new RateLimiter(throttlePerSec);

      for (CSVRecord r : parser) {
        processed++;

        // 헤더가 첫 레코드로 흘러들어오는 케이스 방지
        if (isHeaderEchoRow(r)) {
          skipped++;
          continue;
        }

        try {
          String name = safe(r, colName);
          String phone = safe(r, colPhone);
          String catRaw = safe(r, colCategory);
          String bizType = safe(r, colBizType);

          String roadAddrRaw = safe(r, colAddress);
          String parcelAddrRaw = safe(r, "소재지전체주소");
          String roadAddr = sanitizeAddress(roadAddrRaw);
          String parcelAddr = sanitizeAddress(parcelAddrRaw);

          String xStr = safe(r, colX);
          String yStr = safe(r, colY);
          boolean xBlank = (xStr == null || xStr.isBlank());
          boolean yBlank = (yStr == null || yStr.isBlank());

          // 전통시장류는 제외(중복 로딩 방지)
          if (bizType != null && (bizType.contains("시장") || bizType.contains("상설장"))) {
            filtered++;
            skipped++;
            continue;
          }

          String category = mapCategory(catRaw);
          if (category == null) {
            badCat++;
            skipped++;
            continue;
          }

          if (!StringUtils.hasText(roadAddr)
              && !StringUtils.hasText(parcelAddr)
              && (xBlank || yBlank)) {
            skipped++;
            noAddr++;
            log.warn(
                "[Normalizer] skip row: missing address AND/OR coords: name={}, roadAddr={}, parcelAddr={}, x={}, y={}",
                name,
                roadAddrRaw,
                parcelAddrRaw,
                xStr,
                yStr);
            continue;
          }

          String brand = brandOrNull(name);

          Optional<Coord> point =
              resolveLatLngFrom(r, name, roadAddr, parcelAddr, xStr, yStr, limiter);
          if (point.isEmpty()) {
            noCoord++;
            failed++;
            continue;
          }

          Coord coord = point.get();
          String finalAddr =
              StringUtils.hasText(roadAddr)
                  ? roadAddr
                  : (StringUtils.hasText(parcelAddr) ? parcelAddr : "");

          printer.printRecord(
              nameOrFallback(name),
              brand,
              category,
              finalAddr,
              String.format(Locale.US, "%.8f", coord.lat()),
              String.format(Locale.US, "%.8f", coord.lng()),
              phone);
          ok++;

          if (processed % 50 == 0) {
            log.info(
                "[Normalizer] progress: processed={}, ok={}, skipped={}, failed={}",
                processed,
                ok,
                skipped,
                failed);
          }
        } catch (Exception e) {
          skipped++;
          log.warn("[Normalizer] skip row due to error: {}", e.getMessage());
        }
      }

      log.info(
          "[Normalizer] done: processed={}, ok={}, skipped={}, failed={}, filtered(시장)={}, noAddr={}, badCat={}, noCoord={}, out={}",
          processed,
          ok,
          skipped,
          failed,
          filtered,
          noAddr,
          badCat,
          noCoord,
          outFile.getAbsolutePath());
    }
  }

  private boolean isHeaderEchoRow(CSVRecord r) {
    return r.toMap().entrySet().stream()
        .allMatch(
            e -> {
              String k = clean(e.getKey());
              String v = clean(e.getValue());
              return Objects.equals(k, v);
            });
  }

  private Coord geocodeWithRetry(String address) {
    String addr = sanitizeAddress(address);
    if (!StringUtils.hasText(addr)) {
      return null;
    }

    Coord cached = cache.get(addr);
    if (cached != null) {
      return cached;
    }

    for (int i = 0; i <= maxRetries; i++) {
      var res = geocoder.geocode(addr);
      if (res.isPresent()) {
        Coord c = res.get();
        cache.put(addr, c);
        return c;
      }
      sleep(retryBackoffMs * (i + 1));
    }
    log.warn("[Normalizer] geocode failed after retries: {}", addr);
    return null;
  }

  private Optional<Coord> resolveLatLngFrom(
      CSVRecord r,
      String name,
      String roadAddr,
      String parcelAddr,
      String xStr,
      String yStr,
      RateLimiter limiter) {

    // 1) 좌표가 있으면 좌표 -> WGS84 변환 우선
    Double x = parseDouble(xStr), y = parseDouble(yStr);
    if (x != null && y != null) {
      try {
        Coord transformed = transformToWgs(x, y, originalCrs);
        if (transformed != null) {
          return Optional.of(transformed);
        }
      } catch (UnsupportedOperationException ex) {
        log.warn(
            "[Normalizer] CRS transform not implemented, fallback to geocode (crs={})",
            originalCrs);
      } catch (Exception ex) {
        log.debug(
            "[Normalizer] CRS transform failed: x={}, y={}, crs={}, err={}",
            xStr,
            yStr,
            originalCrs,
            ex.toString());
      }
    }

    // 2) 도로명주소 지오코딩
    if (StringUtils.hasText(roadAddr)) {
      Coord c = cache.get(roadAddr);
      if (c == null) {
        limiter.acquire();
        c = geocodeWithRetry(roadAddr);
      }
      if (c != null) {
        cache.put(roadAddr, c);
        return Optional.of(c);
      }
    }

    // 3) 지번주소 지오코딩
    if (StringUtils.hasText(parcelAddr)) {
      Coord c = cache.get(parcelAddr);
      if (c == null) {
        limiter.acquire();
        c = geocodeWithRetry(parcelAddr);
      }
      if (c != null) {
        cache.put(parcelAddr, c);
        return Optional.of(c);
      }
    }

    // 4) (가게명 + 구/동 힌트) 키워드 지오코딩
    String hint = extractGuDong(StringUtils.hasText(roadAddr) ? roadAddr : parcelAddr);
    if (StringUtils.hasText(name) && StringUtils.hasText(hint)) {
      String keyword = name + " " + hint;
      Coord c = cache.get(keyword);
      if (c == null) {
        limiter.acquire();
        c = geocodeWithRetry(keyword);
      }
      if (c != null) {
        cache.put(keyword, c);
        return Optional.of(c);
      }
    }

    return Optional.empty();
  }

  private Coord transformToWgs(double x, double y, String crs) {
    try {
      if ("EPSG:4326".equalsIgnoreCase(crs)) {
        return new Coord(y, x); // 입력이 이미 lon/lat 순서일 수 있어 방어적으로 lat<-y, lng<-x
      }
      if ("EPSG:5174".equalsIgnoreCase(crs)) {
        double[] lngLat = CrsTransform.toWgs84From5174(x, y);
        return new Coord(lngLat[1], lngLat[0]);
      }
      log.warn("[Normalizer] Unsupported CRS '{}'", crs);
      return null;
    } catch (Exception ex) {
      log.warn("[Normalizer] CRS transform error: {}", ex.toString());
      return null;
    }
  }

  private boolean containsHeader(Map<String, Integer> headerMap, String key) {
    String target = clean(key);
    for (String k : headerMap.keySet()) {
      if (clean(k).equals(target)) {
        return true;
      }
    }
    return false;
  }

  private String nameOrFallback(String name) {
    return (name == null || name.isBlank()) ? "미상" : name.trim();
  }

  private String brandOrNull(String name) {
    String key = (name != null) ? name : "";
    try {
      String code = brandImageResolver.resolveBrandCode(key);
      if (code != null && !"DEFAULT".equalsIgnoreCase(code)) {
        var resolved = brandImageResolver.resolveBrand(key);
        if (resolved != null
            && resolved.displayName() != null
            && !resolved.displayName().isBlank()) {
          return resolved.displayName();
        }
      }
    } catch (Exception ignored) {
    }
    return null;
  }

  private Double parseDouble(String s) {
    try {
      return (s == null || s.isBlank()) ? null : Double.parseDouble(s.trim());
    } catch (Exception e) {
      return null;
    }
  }

  private int require(Map<String, Integer> idx, String key) {
    String target = norm(key);
    Map<String, Integer> normalized = new LinkedHashMap<>();
    for (Map.Entry<String, Integer> e : idx.entrySet()) {
      normalized.put(norm(e.getKey()), e.getValue());
    }
    if (normalized.containsKey(target)) {
      return normalized.get(target);
    }

    for (Map.Entry<String, Integer> e : idx.entrySet()) {
      String k = norm(e.getKey());
      if (k != null && (k.contains(target) || target.contains(k))) {
        return e.getValue();
      }
    }

    StringBuilder sb =
        new StringBuilder("CSV header not found: ").append(key).append(" ; candidates=[");
    boolean first = true;
    for (String k : idx.keySet()) {
      if (!first) {
        sb.append(", ");
      }
      first = false;
      sb.append(k);
    }
    sb.append("]");
    throw new IllegalArgumentException(sb.toString());
  }

  private String safe(CSVRecord r, String col) {
    if (col == null || col.isBlank()) {
      return null;
    }
    String want = norm(col);

    for (String k : r.toMap().keySet()) {
      if (Objects.equals(norm(k), want)) {
        return r.get(k);
      }
    }
    for (String k : r.toMap().keySet()) {
      String nk = norm(k);
      if (nk != null && (nk.contains(want) || want.contains(nk))) {
        return r.get(k);
      }
    }
    return null;
  }

  private String clean(String s) {
    return s == null ? null : s.replace("\uFEFF", "").trim();
  }

  private String norm(String s) {
    if (s == null) {
      return null;
    }
    return s.replace("\uFEFF", "")
        .replaceAll("[\\u200B-\\u200D\\u2060\\uFEFF]", "")
        .trim()
        .replaceAll("[\\s_/\\-\"']", "");
  }

  private String mapCategory(String raw) {
    if (raw == null) {
      return null;
    }
    raw = raw.trim();
    if (raw.contains("대규모점포") || raw.contains("대형마트") || raw.contains("복합쇼핑몰")) {
      return "HYPERMARKET";
    }
    if (raw.contains("슈퍼") || raw.contains("슈퍼마켓")) {
      return "SUPERMARKET";
    }
    // 기타는 하이퍼로 묶어서 저장(스키마 제약)
    return "HYPERMARKET";
  }

  private File resolveOutputFile(Resource res) throws IOException {
    String uri = res.getURI().toString();
    if (uri.startsWith("file:")) {
      return new File(uri.substring("file:".length()));
    }
    return new File("build/marts_normalized.csv");
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
  }

  private static String sanitizeAddress(String raw) {
    if (raw == null) {
      return null;
    }
    String addr = raw;
    addr = addr.replaceAll("\\s+", " ").trim();
    int comma = addr.indexOf(',');
    if (comma > 0 && addr.length() - comma > 3) {
      addr = addr.substring(0, comma).trim();
    }
    if (addr.endsWith(")")) {
      int open = addr.lastIndexOf('(');
      addr =
          (open >= 0)
              ? addr.substring(0, open).trim()
              : addr.substring(0, addr.length() - 1).trim();
    }
    addr = addr.replaceAll("\\([^)]*\\)", "").trim();
    addr = addr.replaceAll("(지하|지상|지층|B\\d+|\\d+층|\\d+호)\\s*$", "").trim();
    addr = addr.replaceAll("[-\\s]+$", "").trim(); // 말미 하이픈/공백 제거
    addr = addr.replaceAll("\\s+", " ").trim();
    return addr;
  }

  private static String extractGuDong(String addr) {
    if (!StringUtils.hasText(addr)) {
      return "";
    }
    try {
      String gu = null, dong = null;
      String[] tokens = addr.split("\\s+");
      for (String t : tokens) {
        if (gu == null && t.endsWith("구")) {
          gu = t;
        }
        if (dong == null && (t.endsWith("동") || t.endsWith("가"))) {
          dong = t;
        }
        if (gu != null && dong != null) {
          break;
        }
      }
      StringBuilder sb = new StringBuilder();
      if (gu != null) {
        sb.append(gu);
      }
      if (dong != null) {
        if (sb.length() > 0) {
          sb.append(' ');
        }
        sb.append(dong);
      }
      return sb.toString().trim();
    } catch (Exception ignored) {
      return "";
    }
  }

  static class RateLimiter {

    private final long intervalNanos;
    private long next = System.nanoTime();

    RateLimiter(int permitsPerSec) {
      this.intervalNanos = (long) (1_000_000_000.0 / Math.max(1, permitsPerSec));
    }

    synchronized void acquire() {
      long now = System.nanoTime();
      if (now < next) {
        long sleepNanos = next - now;
        try {
          Thread.sleep(Duration.ofNanos(sleepNanos).toMillis(), (int) (sleepNanos % 1_000_000));
        } catch (InterruptedException ignored) {
          Thread.currentThread().interrupt();
        }
        next += intervalNanos;
      } else {
        next = now + intervalNanos;
      }
    }
  }

  static class CrsTransform {

    private static final CRSFactory crsFactory = new CRSFactory();
    private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
    private static final CoordinateReferenceSystem EPSG5174 =
        crsFactory.createFromName("EPSG:5174");
    private static final CoordinateReferenceSystem WGS84 = crsFactory.createFromName("EPSG:4326");
    private static final CoordinateTransform TX_5174_TO_4326 =
        ctFactory.createTransform(EPSG5174, WGS84);

    static double[] toWgs84From5174(double x, double y) {
      ProjCoordinate src = new ProjCoordinate(x, y);
      ProjCoordinate dst = new ProjCoordinate();
      TX_5174_TO_4326.transform(src, dst);
      return new double[] {dst.x, dst.y};
    }
  }
}
