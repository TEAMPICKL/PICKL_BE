package com.likelion.picklbe.domain.brand;

import java.util.List;
import java.util.regex.Pattern;

public enum Brand {
  TRADERS("traders", "트레이더스", "traders.png", List.of(p("(이마트\\s*)?트레이더스"), p("traders"))),

  EMART_EVERYDAY(
      "emart-everyday",
      "이마트 에브리데이",
      "emart_everyday.png",
      List.of(p("이마트\\s*에브리데이"), p("emart[-_\\s]*everyday"))),

  LOTTE_FRESH(
      "lotte-fresh", "롯데 프레시", "lotte_fresh.png", List.of(p("롯데\\s*프레시"), p("lotte[-_\\s]*fresh"))),

  LOTTE_SUPER(
      "lotte-super", "롯데슈퍼", "lotte_super.png", List.of(p("롯데\\s*슈퍼"), p("lotte[-_\\s]*super"))),

  LOTTE_MART(
      "lotte-mart", "롯데마트", "lotte_mart.png", List.of(p("롯데\\s*마트"), p("lotte[-_\\s]*mart"))),

  HOMEPLUS("homeplus", "홈플러스", "homeplus.png", List.of(p("홈\\s*플러스"), p("home[-_\\s]*plus"))),

  COSTCO("costco", "코스트코", "costco.png", List.of(p("코스트코"), p("costco"))),

  HANARO("hanaro", "하나로마트", "hanaro.png", List.of(p("(농협|하나로)\\s*마트"), p("하나로클럽"), p("hanaro"))),

  EMART(
      "emart",
      "이마트",
      "emart.png",
      List.of(
          // 에브리데이/트레이더스/이마트24 제외
          p("\\b이마트\\b(?!\\s*에브리데이)(?!\\s*트레이더스)(?!\\s*24)"),
          p("\\bemart\\b(?![-_\\s]*everyday)(?![-_\\s]*traders)(?![-_\\s]*24)"))),

  NO_BRAND("no-brand", "노브랜드", "no_brand.png", List.of(p("노\\s*브랜드"), p("no[-_\\s]*brand"))),

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

  public String displayName() {
    return displayName;
  }

  public String filename() {
    return filename;
  }

  public static Brand fromStoreName(String name) {
    if (name == null || name.isBlank()) {
      return DEFAULT;
    }

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

  // 공통 플래그
  private static Pattern p(String regex) {
    return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  }
}
