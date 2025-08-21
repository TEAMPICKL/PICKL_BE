package com.likelion.picklbe.domain.mart.entity;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "place")
public class Place {

  public enum Category {
    HYPERMARKET,
    SUPERMARKET
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 60)
  private String brand; // 이마트/홈플/롯데/코스트코/노브랜드 등

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Category category;

  @Column(length = 255)
  private String address;

  @Column(nullable = false)
  private Double lat;

  @Column(nullable = false)
  private Double lng;

  // MySQL Geometry — POINT(lng,lat), SRID 4326
  @Column(nullable = false, columnDefinition = "point SRID 4326")
  private Point location;

  @Column(
      name = "created_at",
      updatable = false,
      insertable = false,
      columnDefinition = "timestamp default current_timestamp")
  private Instant createdAt;

  @UpdateTimestamp
  @Column(
      name = "updated_at",
      insertable = false,
      columnDefinition = "timestamp default current_timestamp on update current_timestamp")
  private Instant updatedAt;
}
