package com.likelion.picklbe.domain.chatbot.util;

import java.util.regex.Pattern;

public class TextPostProcessor {

  private static final Pattern DAY_HEADER_ANYWHERE =
      Pattern.compile("\\s*-\\s*\\*\\*(월요일|화요일|수요일|목요일|금요일|토요일|일요일)\\*\\*:");

  public static String prettyWeeklyPlan(String text) {
    if (text == null || text.isBlank()) {
      return text;
    }
    String out = text;

    // 1) 요일 헤더가 문장 중간에 붙어 나오면 줄바꿈으로 정규화
    out = DAY_HEADER_ANYWHERE.matcher(out).replaceAll("\n- **$1**:");

    // 2) 모든 요일 헤더 앞에 빈 줄 하나(첫 번째 헤더 앞 공백은 제거)
    out = out.replaceAll("(?m)\\n- \\*\\*(월요일|화요일|수요일|목요일|금요일|토요일|일요일)\\*\\*:", "\n\n- **$1**:");
    out = out.replaceFirst("^\\n+", "");

    // 3) 끼니/운동 소항목은 각 줄로 (줄 시작이 아닐 때만 개행 추가)
    out = out.replaceAll("(?<!\\n)-\\s*(아침|점심|저녁|스낵|간식|운동)\\s*:", "\n  - $1:");

    // 4) 필요 이상 공백 정리
    out = out.replaceAll(" {2,}", " ");

    return out.trim();
  }
}
