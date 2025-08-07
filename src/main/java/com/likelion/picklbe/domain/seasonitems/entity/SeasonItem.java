package com.likelion.picklbe.domain.seasonitems.entity;

import java.util.List;

import jakarta.persistence.*;

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

  @Column(name = "carbohydratePercent", nullable = false)
  private int carbohydratePercent;

  @Column(name = "proteinPercent", nullable = false)
  private int proteinPercent;

  @Column(name = "fatPercent", nullable = false)
  private int fatPercent;

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

  @Column(name = "imageUrl", nullable = false)
  private String imageUrl;

  @OneToMany(mappedBy = "seasonItem", cascade = CascadeType.ALL)
  private List<Recipe> recommendedRecipes;
}
