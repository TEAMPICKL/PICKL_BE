package com.likelion.picklbe.domain.brand;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public enum Brand {
  EMART_EVERYDAY(
      "emart-everyday",
      "이마트 에브리데이",
      "emart_everyday.png",
      List.of(
          Pattern.compile("이마트\\s*에브리데이"),
          Pattern.compile("emart\\s*everyday", Pattern.CASE_INSENSITIVE))),
  NO_BRAND(
      "no-brand",
      "노브랜드",
      "no_brand.png",
      List.of(
          Pattern.compile("노\\s*브랜드"), Pattern.compile("no\\s*brand", Pattern.CASE_INSENSITIVE))),
  EMART(
      "emart",
      "이마트",
      "emart.png",
      List.of(
          Pattern.compile("\\b이마트\\b"), Pattern.compile("\\bemart\\b", Pattern.CASE_INSENSITIVE))),
  HOMEPLUS(
      "homeplus",
      "홈플러스",
      "homeplus.png",
      List.of(
          Pattern.compile("홈\\s*플러스"), Pattern.compile("home\\s*plus", Pattern.CASE_INSENSITIVE))),
  COSTCO(
      "costco",
      "코스트코",
      "costco.png",
      List.of(Pattern.compile("코스트코"), Pattern.compile("costco", Pattern.CASE_INSENSITIVE))),
  LOTTE_MART(
      "lotte-mart",
      "롯데마트",
      "lotte_mart.png",
      List.of(
          Pattern.compile("롯데\\s*마트"), Pattern.compile("lotte\\s*mart", Pattern.CASE_INSENSITIVE))),
  LOTTE_SUPER(
      "lotte-super",
      "롯데슈퍼",
      "lotte_super.png",
      List.of(
          Pattern.compile("롯데\\s*슈퍼"),
          Pattern.compile("lotte\\s*super", Pattern.CASE_INSENSITIVE))),
  LOTTE_FRESH(
      "lotte-fresh",
      "롯데프레시",
      "lotte_fresh.png",
      List.of(
          Pattern.compile("롯데\\s*프레시"),
          Pattern.compile("lotte\\s*fresh", Pattern.CASE_INSENSITIVE))),
  TRADERS(
      "traders",
      "트레이더스",
      "traders.png",
      List.of(Pattern.compile("트레이더스"), Pattern.compile("traders", Pattern.CASE_INSENSITIVE))),
  HANARO(
      "hanaro",
      "하나로마트",
      "hanaro.png",
      List.of(
          Pattern.compile("(농협|하나로)\\s*마트"),
          Pattern.compile("하나로클럽"),
          Pattern.compile("hanaro", Pattern.CASE_INSENSITIVE))),
  GS_SUPER(
      "gs-super",
      "GS슈퍼",
      "gs_super.png",
      List.of(
          Pattern.compile("지에스리테일"),
          Pattern.compile("\\bGS\\b\\s*(THE\\s*FRESH|슈퍼)?", Pattern.CASE_INSENSITIVE))),
  DEFAULT("default", "기타", "mart_default.png", List.of());

  private final String code;
  private final String displayName;
  private final String filename;
  private final List<Pattern> patterns;

  Brand(String code, String displayName, String filename, List<Pattern> patterns) {
    this.code = code;
    this.displayName = displayName;
    this.filename = filename;
    this.patterns = patterns;
  }

  public String code() {
    return code;
  }

  public String filename() {
    return filename;
  }

  public String displayName() {
    return displayName;
  }

  private static final Map<String, Brand> BY_CODE;

  static {
    Map<String, Brand> m = new HashMap<>();
    for (Brand b : values()) {
      m.put(b.code().toLowerCase(Locale.ROOT), b);
    }
    BY_CODE = Collections.unmodifiableMap(m);
  }

  public static Brand fromCodeSafe(String code) {
    if (code == null || code.isBlank()) {
      return DEFAULT;
    }
    Brand b = BY_CODE.get(code.toLowerCase(Locale.ROOT));
    return (b != null) ? b : DEFAULT;
  }

  public static Brand fromStoreName(String name) {
    if (name == null || name.isBlank()) {
      return DEFAULT;
    }
    // 우선순위: 에브리데이 → GS → 이마트 등 (선언 순서 유지)
    for (Brand b : values()) {
      if (b == DEFAULT) {
        continue;
      }
      for (Pattern p : b.patterns) {
        if (p.matcher(name).find()) {
          return b;
        }
      }
    }
    return DEFAULT;
  }
}
