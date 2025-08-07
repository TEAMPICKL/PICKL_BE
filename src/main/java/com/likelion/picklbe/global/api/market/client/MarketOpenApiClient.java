package com.likelion.picklbe.global.api.market.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

@Service
public class MarketOpenApiClient {

  private static final String SERVICE_KEY =
      "zTQoSerFeqqlMDuJI4uZg5AERDK5AMbL8caK5M0FL6Ou1PZifFYtQF0kGBAD9uMxF8Y%2BQHDtR78b1wyW7Qbkig%3D%3D";

  public String fetchMarkets() {
    try {
      String url =
          "https://apis.data.go.kr/6260000/MarketService/getMarketList"
              + "?serviceKey="
              + SERVICE_KEY
              + "&pageNo=1"
              + "&numOfRows=100"
              + "&type=json";

      HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
      conn.setRequestMethod("GET");

      BufferedReader rd =
          new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
      StringBuilder result = new StringBuilder();
      String line;
      while ((line = rd.readLine()) != null) {
        result.append(line);
      }
      rd.close();
      conn.disconnect();

      return result.toString();

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
