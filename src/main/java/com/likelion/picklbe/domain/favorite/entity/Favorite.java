package com.likelion.picklbe.domain.favorite.entity;

import jakarta.persistence.*;

import com.likelion.picklbe.domain.user.entity.User;

import lombok.*;

@Entity
@Table(
    name = "favorites",
    indexes = {@Index(name = "idx_fav_user_type", columnList = "user_id,type")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private FavoriteType type; // INGREDIENT / RECIPE

  @Column(name = "target_id", nullable = false)
  private Long targetId; // 찜 대상 PK (재료ID/레시피ID)

  public enum FavoriteType {
    INGREDIENT,
    RECIPE
  }
}
