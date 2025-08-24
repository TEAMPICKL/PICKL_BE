package com.likelion.picklbe.domain.period.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.picklbe.domain.dailypricechange.repository.ItemKindCatView;
import com.likelion.picklbe.domain.dailypricechange.repository.KamisItemPriceRepository;
import com.likelion.picklbe.global.api.kamis.client.KamisPeriodClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KamisPeriodService {

  private final KamisPeriodClient client;
  private final KamisItemPriceRepository dailyRepo;

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final ObjectMapper OM = new ObjectMapper();

  // ======== [여기부터] 정적 코드 테이블 ========
  /** (부류코드, 품목코드) — 앞 2열만 파싱 */
  private static final String CATEGORY_ITEM_DATA =
      """
          100 111 쌀
          100 112 찹쌀
          100 113 혼합곡
          100 114 기장
          100 141 콩
          100 142 팥
          100 143 녹두
          100 144 메밀
          100 151 고구마
          100 152 감자
          100 161 귀리
          100 162 보리
          100 163 수수
          100 164 율무
          200 211 배추
          200 212 양배추
          200 213 시금치
          200 214 상추
          200 215 얼갈이배추
          200 216 갓
          200 217 연근
          200 218 우엉
          200 221 수박
          200 222 참외
          200 223 오이
          200 224 호박
          200 225 토마토
          200 226 딸기
          200 231 무
          200 232 당근
          200 233 열무
          200 241 건고추
          200 242 풋고추
          200 243 붉은고추
          200 244 피마늘
          200 245 양파
          200 246 파
          200 247 생강
          200 248 고춧가루
          200 251 가지
          200 252 미나리
          200 253 깻잎
          200 254 부추
          200 255 피망
          200 256 파프리카
          200 257 멜론
          200 258 깐마늘(국산)
          200 259 깐마늘(수입)
          200 261 브로콜리
          200 262 양상추
          200 263 청경채
          200 264 케일
          200 265 콩나물
          200 266 절임배추
          200 267 쑥
          200 268 달래
          200 269 두릅
          200 270 로메인상추
          200 271 취나물
          200 272 쥬키니호박
          200 273 청양고추
          200 274 대파
          200 275 고사리
          200 276 쪽파
          200 277 다발무
          200 278 겨울배추
          200 279 알배기배추
          200 280 브로콜리
          200 422 방울토마토
          300 312 참깨
          300 313 들깨
          300 314 땅콩
          300 315 느타리버섯
          300 316 팽이버섯
          300 317 새송이버섯
          300 318 호두
          300 319 아몬드
          300 321 양송이버섯
          300 322 표고버섯
          300 323 더덕
          400 411 사과
          400 412 배
          400 413 복숭아
          400 414 포도
          400 415 감귤
          400 416 단감
          400 418 바나나
          400 419 참다래
          400 420 파인애플
          400 421 오렌지
          400 423 자몽
          400 424 레몬
          400 425 체리
          400 426 건포도
          400 427 건블루베리
          400 428 망고
          400 429 블루베리
          400 430 아보카도
          400 431 레드향
          400 432 매실
          400 433 무화과
          400 434 복분자
          400 435 샤인머스켓
          400 436 곶감
          400 437 골드키위
          600 611 고등어
          600 612 꽁치
          600 613 갈치
          600 614 조기
          600 615 명태
          600 616 삼치
          600 619 물오징어
          600 638 건멸치
          600 639 북어
          600 640 건오징어
          600 641 김
          600 642 건미역
          600 644 굴
          600 649 수입조기
          600 650 새우젓
          600 651 멸치액젓
          600 652 굵은소금
          600 653 전복
          600 654 새우
          600 655 주꾸미
          600 656 꽃게
          600 657 참조기
          600 658 홍합
          600 659 가리비
          600 660 건다시마
          """;

  /** (품목코드, 품종코드) — 앞 2열만 파싱 */
  private static final String ITEM_KIND_DATA =
      """
          111 01 쌀
          111 02 쌀
          111 03 쌀
          111 05 쌀
          111 06 쌀
          111 07 쌀
          111 08 쌀
          111 09 쌀
          111 10 쌀
          111 11 쌀
          112 01 찹쌀
          113 00 혼합곡
          114 01 기장
          141 01 콩
          141 02 콩
          141 03 콩
          142 00 팥
          142 01 팥
          143 00 녹두
          143 01 녹두
          144 01 메밀
          151 00 고구마
          152 00 감자
          152 01 감자
          152 02 감자
          152 03 감자
          152 04 감자
          152 05 감자
          152 06 감자
          161 01 귀리
          162 01 보리
          163 01 수수
          164 01 율무
          211 01 배추
          211 02 배추
          211 03 배추
          211 06 배추
          212 00 양배추
          213 00 시금치
          214 01 상추
          214 02 상추
          215 00 얼갈이배추
          216 00 갓
          217 01 연근
          218 01 우엉
          221 00 수박
          222 00 참외
          223 01 오이
          223 02 오이
          223 03 오이
          224 01 호박
          224 02 호박
          224 03 호박
          225 00 토마토
          226 00 딸기
          231 01 무
          231 02 무
          231 03 무
          231 06 무
          232 00 당근
          232 01 당근
          232 02 당근
          232 10 당근
          233 00 열무
          241 00 건고추
          241 01 건고추
          241 02 건고추
          241 03 건고추
          241 10 건고추
          241 90 건고추
          241 99 건고추
          242 00 풋고추
          242 02 풋고추
          242 03 풋고추
          242 04 풋고추
          243 00 붉은고추
          244 01 피마늘
          244 02 피마늘
          244 03 피마늘
          244 04 피마늘
          244 06 피마늘
          244 07 피마늘
          244 08 피마늘
          244 21 피마늘
          244 22 피마늘
          244 23 피마늘
          244 24 피마늘
          245 00 양파
          245 02 양파
          245 10 양파
          246 00 파
          246 02 파
          247 00 생강
          247 01 생강
          248 00 고춧가루
          248 01 고춧가루
          251 00 가지
          252 00 미나리
          253 00 깻잎
          254 00 부추
          255 00 피망
          256 00 파프리카
          257 00 멜론
          258 01 깐마늘(국산)
          258 03 깐마늘(국산)
          258 04 깐마늘(국산)
          258 05 깐마늘(국산)
          258 06 깐마늘(국산)
          259 01 깐마늘(수입)
          259 03 깐마늘(수입)
          261 01 브로콜리
          262 01 양상추
          263 01 청경채
          264 01 케일
          265 01 콩나물
          266 00 절임배추
          266 01 절임배추
          266 02 절임배추
          266 03 절임배추
          266 04 절임배추
          276 02 쪽파
          279 00 알배기배추
          280 00 브로콜리
          312 01 참깨
          312 02 참깨
          312 03 참깨
          313 01 들깨
          313 02 들깨
          314 01 땅콩
          314 02 땅콩
          315 00 느타리버섯
          315 01 느타리버섯
          316 00 팽이버섯
          317 00 새송이버섯
          318 00 호두
          319 00 아몬드
          321 01 양송이버섯
          322 01 표고버섯
          411 01 사과
          411 05 사과
          411 06 사과
          411 07 사과
          412 01 배
          412 02 배
          412 03 배
          412 04 배
          413 01 복숭아
          413 04 복숭아
          413 05 복숭아
          414 01 포도
          414 02 포도
          414 03 포도
          414 06 포도
          414 07 포도
          414 08 포도
          414 09 포도
          414 10 포도
          414 11 포도
          414 12 포도
          415 00 감귤
          415 01 감귤
          415 02 감귤
          416 00 단감
          418 02 바나나
          419 01 참다래
          419 02 참다래
          420 02 파인애플
          421 02 오렌지
          421 03 오렌지
          421 04 오렌지
          421 05 오렌지
          421 06 오렌지
          422 01 방울토마토
          422 02 방울토마토
          423 00 자몽
          424 00 레몬
          425 00 체리
          426 00 건포도
          427 00 건블루베리
          428 00 망고
          429 01 블루베리
          430 00 아보카도
          611 01 고등어
          611 02 고등어
          611 03 고등어
          611 04 고등어
          611 05 고등어
          611 06 고등어
          611 07 고등어
          611 08 고등어
          612 01 꽁치
          613 01 갈치
          613 02 갈치
          613 03 갈치
          613 04 갈치
          613 05 갈치
          614 01 조기
          614 04 조기
          614 05 조기
          614 06 조기
          614 07 조기
          615 01 명태
          615 02 명태
          615 03 명태
          615 04 명태
          615 05 명태
          616 02 삼치
          619 01 물오징어
          619 02 물오징어
          619 03 물오징어
          619 04 물오징어
          619 05 물오징어
          638 00 건멸치
          639 01 북어
          639 02 북어
          640 00 건오징어
          641 00 김
          641 01 김
          642 00 건미역
          644 00 굴
          649 01 수입조기
          649 04 수입조기
          650 00 새우젓
          651 00 멸치액젓
          652 00 굵은소금
          653 00 전복
          654 01 새우
          657 01 참조기
          658 01 홍합
          658 02 홍합
          659 01 가리비
          660 01 건다시마
          """;

  // ======== [여기까지] 정적 코드 테이블 ========

  /** 단일 조합 테스트 호출 */
  @Transactional(readOnly = true)
  public Map<String, Object> previewOne(
      String startDayOpt,
      String endDayOpt,
      String marketOpt,
      String categoryCode,
      String itemCode,
      String kindCode,
      String productRankOpt,
      String countyCodeOpt) {
    String start = (startDayOpt == null || startDayOpt.isBlank()) ? "2025-08-01" : startDayOpt;
    String end =
        (endDayOpt == null || endDayOpt.isBlank()) ? LocalDate.now(KST).toString() : endDayOpt;
    String cls = toCls(marketOpt);
    String rank = (productRankOpt == null || productRankOpt.isBlank()) ? "04" : productRankOpt;
    String county = (countyCodeOpt == null || countyCodeOpt.isBlank()) ? "1101" : countyCodeOpt;

    if (isBlank(categoryCode) || isBlank(itemCode) || isBlank(kindCode)) {
      return Map.of("status", "bad-request", "message", "category/item/kind 필수");
    }

    String payload = null;
    String code = "EXCEPTION";
    try {
      payload =
          client.fetchPeriodProductListRaw(
              start, end, cls, categoryCode, itemCode, kindCode, rank, county, "N");
      code = extractErrorCode(payload);
    } catch (Exception e) {
      log.error("[period-one] exception", e);
    }

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("status", "ok");
    out.put("startDay", start);
    out.put("endDay", end);
    out.put("productClsCode", cls);
    out.put("categoryCode", categoryCode);
    out.put("itemCode", itemCode);
    out.put("kindCode", kindCode);
    out.put("productRankCode", rank);
    out.put("countyCode", county);
    out.put("ok", "000".equals(code));
    out.put("error_code", code);
    out.put("payload", payload == null ? "" : payload);
    return out;
  }

  /** 전수 스캔: source=daily(기본) | static(정적 테이블) */
  @Transactional(readOnly = true)
  public Map<String, Object> previewAll(
      String startDayOpt,
      String endDayOpt,
      String categoryFilterOpt,
      String marketOpt,
      String productRankOpt,
      String countyCodeOpt,
      String sourceOpt // "daily"(default) | "static"
      ) {
    String start = (startDayOpt == null || startDayOpt.isBlank()) ? "2025-08-01" : startDayOpt;
    String end =
        (endDayOpt == null || endDayOpt.isBlank()) ? LocalDate.now(KST).toString() : endDayOpt;
    String rank = (productRankOpt == null || productRankOpt.isBlank()) ? "04" : productRankOpt;
    String county = (countyCodeOpt == null || countyCodeOpt.isBlank()) ? "1101" : countyCodeOpt;
    List<String> clsList = resolveClsList(marketOpt);

    // (1) 조합 소스 선택
    List<Pair> pairs;
    String source = (sourceOpt == null || sourceOpt.isBlank()) ? "daily" : sourceOpt;
    if ("static".equalsIgnoreCase(source)) {
      pairs = loadPairsFromStaticTables(categoryFilterOpt);
    } else {
      pairs = loadPairsFromDailyTables(categoryFilterOpt);
      if (pairs.isEmpty()) { // fallback
        log.info("[period-all] no pairs from daily, fallback to static tables");
        pairs = loadPairsFromStaticTables(categoryFilterOpt);
        source = "static";
      }
    }
    if (pairs.isEmpty()) {
      Map<String, Object> o = new LinkedHashMap<>();
      o.put("status", "no-pairs");
      o.put("source", source);
      return o;
    }

    // (2) 호출
    List<Map<String, Object>> items = new ArrayList<>();
    int ok = 0, err = 0;

    for (Pair p : pairs) {
      for (String cls : clsList) {
        String payload = null;
        String code = "EXCEPTION";
        try {
          payload =
              client.fetchPeriodProductListRaw(
                  start, end, cls, p.category, p.item, p.kind, rank, county, "N");
          code = extractErrorCode(payload);
        } catch (Exception e) {
          log.warn(
              "[period-all] exception for {}/{}/{}, cls={}", p.category, p.item, p.kind, cls, e);
        }
        boolean success = "000".equals(code);
        if (success) {
          ok++;
        } else {
          err++;
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("productClsCode", cls);
        row.put("categoryCode", p.category);
        row.put("itemCode", p.item);
        row.put("kindCode", p.kind);
        row.put("ok", success);
        row.put("error_code", code);
        row.put("payload", payload == null ? "" : payload);
        items.add(row);
      }
    }

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("status", "ok");
    out.put("mode", "period-scan");
    out.put("source", source);
    out.put("startDay", start);
    out.put("endDay", end);
    out.put("categoryFilter", categoryFilterOpt == null ? "ALL" : categoryFilterOpt);
    out.put("productRankCode", rank);
    out.put("countyCode", county);
    out.put("markets", clsList);
    out.put("success", ok);
    out.put("errors", err);
    out.put("count", items.size());
    out.put("items", items);
    return out;
  }

  // ===== helpers =====

  private static record Pair(String category, String item, String kind) {}

  /** 일일테이블에서 (cat,item,kind) 추출 → Pair 리스트 */
  private List<Pair> loadPairsFromDailyTables(String categoryFilterOpt) {
    LocalDate latest = dailyRepo.findLatestPriceDate();
    if (latest == null) {
      return List.of();
    }

    List<Pair> acc = new ArrayList<>();
    for (String cls : new String[] {"01", "1", "02", "2", null}) {
      try {
        List<ItemKindCatView> part =
            dailyRepo.findDistinctItemKindPairs(latest, categoryFilterOpt, cls);
        if (part != null) {
          for (ItemKindCatView v : part) {
            acc.add(new Pair(nz(v.getCategoryCode()), nz(v.getItemCode()), nz(v.getKindCode())));
          }
        }
      } catch (Exception e) {
        log.debug("[pairs-daily] fail cls={}", cls, e);
      }
    }
    return dedupPairs(acc);
  }

  /** 정적 테이블에서 (cat,item,kind) 전수 생성 → Pair 리스트 */
  private List<Pair> loadPairsFromStaticTables(String categoryFilterOpt) {
    // 1) (cat,item) 목록
    List<String[]> catItems = parseTwoCols(CATEGORY_ITEM_DATA);
    if (categoryFilterOpt != null && !categoryFilterOpt.isBlank()) {
      catItems =
          catItems.stream()
              .filter(ci -> categoryFilterOpt.equals(ci[0]))
              .collect(Collectors.toList());
    }
    // 2) item -> kinds 맵
    Map<String, List<String>> itemToKinds = parseItemToKinds(ITEM_KIND_DATA);

    // 3) 조합 생성
    List<Pair> out = new ArrayList<>();
    for (String[] ci : catItems) {
      String cat = ci[0], item = ci[1];
      List<String> kinds = itemToKinds.get(item);
      if (kinds == null || kinds.isEmpty()) {
        continue;
      }
      for (String kind : kinds) {
        out.add(new Pair(cat, item, kind));
      }
    }
    return dedupPairs(out);
  }

  private static List<String[]> parseTwoCols(String raw) {
    List<String[]> out = new ArrayList<>();
    for (String line : raw.split("\\R")) {
      String s = line.trim();
      if (s.isEmpty() || s.startsWith("#")) {
        continue;
      }
      String[] tok = s.split("\\s+");
      if (tok.length >= 2) {
        out.add(new String[] {tok[0], tok[1]});
      }
    }
    return out;
  }

  private static Map<String, List<String>> parseItemToKinds(String raw) {
    Map<String, List<String>> map = new LinkedHashMap<>();
    for (String line : raw.split("\\R")) {
      String s = line.trim();
      if (s.isEmpty() || s.startsWith("#")) {
        continue;
      }
      String[] tok = s.split("\\s+");
      if (tok.length >= 2) {
        String item = tok[0], kind = tok[1];
        map.computeIfAbsent(item, k -> new ArrayList<>()).add(kind);
      }
    }
    // 중복 제거(순서 유지)
    map.replaceAll((k, v) -> new ArrayList<>(new LinkedHashSet<>(v)));
    return map;
  }

  private static List<Pair> dedupPairs(List<Pair> list) {
    LinkedHashMap<String, Pair> uniq = new LinkedHashMap<>();
    for (Pair p : list) {
      uniq.put(p.category + "|" + p.item + "|" + p.kind, p);
    }
    return new ArrayList<>(uniq.values());
  }

  private static String nz(String s) {
    return s == null ? "" : s.trim();
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  private static String toCls(String market) {
    if ("01".equals(market) || "1".equals(market) || "소매".equals(market)) {
      return "01";
    }
    if ("02".equals(market) || "2".equals(market) || "도매".equals(market)) {
      return "02";
    }
    return "01";
  }

  private static List<String> resolveClsList(String marketOpt) {
    if (marketOpt == null || marketOpt.isBlank()) {
      return List.of("01", "02");
    }
    return List.of(toCls(marketOpt));
  }

  /** JSON 우선( data.error_code 도 지원 ), 실패 시 XML에서 <error_code> 파싱 */
  private static String extractErrorCode(String payload) {
    if (payload == null || payload.isBlank()) {
      return "EXCEPTION";
    }
    try {
      JsonNode n = OM.readTree(payload);
      JsonNode ec = n.path("error_code");
      if (ec.isMissingNode() || ec.isNull()) {
        ec = n.path("data").path("error_code");
      }
      if (!ec.isMissingNode() && !ec.isNull()) {
        return ec.asText();
      }
    } catch (Exception ignore) {
    }
    try {
      Matcher m = Pattern.compile("<error_code>(\\d+)</error_code>").matcher(payload);
      if (m.find()) {
        return m.group(1);
      }
    } catch (Exception ignore) {
    }
    return "UNKNOWN";
  }
}
