package com.likelion.picklbe.domain.seasonitems.recipe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.likelion.picklbe.domain.seasonitems.entity.SeasonItem;

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
@Table(name = "season_item_recipe")
public class Recipe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // ✅ DB 컬럼명이 recipe_name 이므로 snake_case 로 맞춰야 함
  @Column(name = "recipe_name", nullable = false)
  private String recipeName;

  @Lob
  @Column(name = "ingredients", nullable = false)
  private String ingredients;

  @Lob
  @Column(name = "instructions", nullable = false)
  private String instructions;

  @Lob
  @Column(name = "tip", nullable = false)
  private String tip;

  @Column(name = "cooking_time_text")
  private String cookingTimeText;

  @Column(name = "recommend_tags_csv")
  private String recommendTagsCsv;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "season_item_id", nullable = false)
  private SeasonItem seasonItem; // ✅ 연관 엔티티 자체를 들고 있음 (ID 아님)
}
