package com.likelion.picklbe.global.util;

import com.likelion.picklbe.infra.geo.VWorldGeocoder;

import org.locationtech.proj4j.*;

public class CrsTransform {

  private static final CRSFactory FACTORY = new CRSFactory();
  private static final CoordinateReferenceSystem WGS84 = FACTORY.createFromName("EPSG:4326");
  private static final CoordinateReferenceSystem KOREA_TM_5179 =
      FACTORY.createFromName("EPSG:5179");
  private static final CoordinateReferenceSystem KOREA_TM_5174 =
      FACTORY.createFromName("EPSG:5174");

  private static final CoordinateTransformFactory CTF = new CoordinateTransformFactory();
  private static final CoordinateTransform TM5179_TO_WGS =
      CTF.createTransform(KOREA_TM_5179, WGS84);
  private static final CoordinateTransform TM5174_TO_WGS =
      CTF.createTransform(KOREA_TM_5174, WGS84);

  /** TM → WGS 변환 (x=TM_X, y=TM_Y). crs 는 "EPSG:5179" 또는 "EPSG:5174" */
  public static VWorldGeocoder.Coord tmToWgs(double x, double y, String crs) {
    ProjCoordinate src = new ProjCoordinate(x, y);
    ProjCoordinate dst = new ProjCoordinate();

    if ("EPSG:5179".equalsIgnoreCase(crs)) {
      TM5179_TO_WGS.transform(src, dst);
    } else if ("EPSG:5174".equalsIgnoreCase(crs)) {
      TM5174_TO_WGS.transform(src, dst);
    } else if ("EPSG:4326".equalsIgnoreCase(crs)) {
      // 이미 WGS면 그대로 사용
      dst.x = x;
      dst.y = y;
    } else {
      throw new IllegalArgumentException("Unsupported CRS: " + crs);
    }

    // proj4j: dst.x=lng, dst.y=lat
    return new VWorldGeocoder.Coord(dst.y, dst.x);
  }
}
