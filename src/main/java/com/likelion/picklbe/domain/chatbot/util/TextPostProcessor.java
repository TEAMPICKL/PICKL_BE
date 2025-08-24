package com.likelion.picklbe.domain.chatbot.util;

import java.util.regex.Pattern;

public class TextPostProcessor {

  // 요일 헤더가 문장 중간에 섞여 있으면 줄바꿈으로 분리
  private static final Pattern DAY_HEADER_ANYWHERE =
      Pattern.compile("\\s*-\\s*\\*\\*(월요일|화요일|수요일|목요일|금요일|토요일|일요일)\\*\\*:");

  // ",운동" → ", 운동" 같은 콤마 뒤 공백 보정
  private static final Pattern COMMA_TIGHT =
      Pattern.compile(",(?=\\S)");

  // "운동30분" / ",운동30" / "운동 30 분" 등을 ", 운동 30분"으로 정규화
  private static final Pattern EXERCISE_TIGHT =
      Pattern.compile("(?i)(,?)\\s*운동\\s*(\\d+)\\s*분?");

  // 요일 라인이 다른 줄과 붙어 있으면 개행 보장
  private static final Pattern DAY_LINE_NEEDS_LF =
      Pattern.compile("(?m)(^\\s*-\\s*\\*\\*[월화수목금토일]요일\\*\\*\\s*:.*)(?!\\n)");

  // 한글/영문/닫는 괄호 뒤에 숫자가 바로 오면 숫자 앞에 공백 1칸 삽입 (예: "걷기30분"→"걷기 30분")
  private static final Pattern SPACE_BEFORE_DIGIT =
      Pattern.compile("(?<=[가-힣A-Za-z)])(?=\\d)");

  public static String prettyWeeklyPlan(String text) {
    if (text == null || text.isBlank()) {
      return text;
    }
    String out = text.replace("\r\n", "\n").replace("\r", "\n");

    // 1) 요일 헤더를 줄 시작으로 강제
    out = DAY_HEADER_ANYWHERE.matcher(out).replaceAll("\n- **$1**:");

    // 2) 각 요일 헤더 앞에 빈 줄 하나(첫 헤더는 제외)
    out = out.replaceAll("(?m)\\n- \\*\\*(월요일|화요일|수요일|목요일|금요일|토요일|일요일)\\*\\*:", "\n\n- **$1**:");
    out = out.replaceFirst("^\\n+", "");

    // 3) 콤마 뒤 공백 보정
    out = COMMA_TIGHT.matcher(out).replaceAll(", ");

    // 4) '운동' 표기 뭉침 보정 → ", 운동 30분"
    out = EXERCISE_TIGHT.matcher(out).replaceAll(", 운동 $2분");

    // 5) 요일 라인 끝에 개행 보장(다음 헤더/문장과 붙는 현상 방지)
    out = DAY_LINE_NEEDS_LF.matcher(out).replaceAll("$1\n");

    // 6) 끼니/운동 소항목은 각 줄로 (줄 시작이 아닐 때만 개행 추가)
    out = out.replaceAll("(?<!\\n)-\\s*(아침|점심|저녁|스낵|간식|운동)\\s*:", "\n  - $1:");

    // 7) 숫자 앞 공백 강제
    out = SPACE_BEFORE_DIGIT.matcher(out).replaceAll(" ");

    // 8) 다중 공백 정리
    out = out.replaceAll(" {2,}", " ");

    return out.trim();
  }
}