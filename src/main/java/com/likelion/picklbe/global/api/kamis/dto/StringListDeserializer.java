package com.likelion.picklbe.global.api.kamis.dto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

public class StringListDeserializer extends JsonDeserializer<List<String>> {
  @Override
  public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    List<String> list = new ArrayList<>();
    JsonToken curr = p.getCurrentToken();

    if (curr == JsonToken.START_ARRAY) {
      // 배열이면 끝날 때까지 읽어서 추가
      while (p.nextToken() != JsonToken.END_ARRAY) {
        list.add(p.getValueAsString());
      }
    } else {
      // 단일 값이면 하나만 리스트에 담기
      list.add(p.getValueAsString());
    }

    return list;
  }
}
