package com.likelion.picklbe.domain.seasonitems.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.likelion.picklbe.domain.seasonitems.recipe.entity.Recipe;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "SeasonItem")
public class SeasonItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "itemname", nullable = false)
  private String itemname;

  @Column(name = "shortDescription", nullable = false)
  private String shortDescription;

  @Column(name = "calorie", nullable = false)
  private String calorie; // 칼로리

  @Column(name = "representativeNutrient", nullable = false)
  private String representativeNutrient; // 대표 영양소

  @Column(name = "inSeasonMonth", nullable = false)
  private Integer inSeasonMonth; // 제철인 달

  @Lob
  @Column(name = "howToChoose", nullable = false)
  private String howToChoose; // 고르는 방법

  @Lob
  @Column(name = "howToStore", nullable = false)
  private String howToStore; // 보관하는 방법

  @Lob
  @Column(name = "howToTrim", nullable = false)
  private String howToTrim; // 손질하는 방법

  @Lob
  @Column(name = "tip", nullable = false)
  private String tip; // 꿀팁

  @Column(name = "unit", nullable = false, length = 50)
  private String unit;

  @Column(name = "price", nullable = false)
  private Integer price;

  @Column(name = "imageUrl", nullable = false)
  private String imageUrl;

  @OneToMany(mappedBy = "seasonItem", cascade = CascadeType.ALL)
  private List<Recipe> recommendedRecipes;
}
