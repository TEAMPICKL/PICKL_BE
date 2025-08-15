package com.likelion.picklbe.domain.favorite.entity;

import jakarta.persistence.*;

import com.likelion.picklbe.domain.user.entity.User;

import lombok.*;

@Entity
@Table(
    name = "favorites",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_fav_user_type_target",
          columnNames = {"user_id", "type", "target_id"})
    },
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
  private Long targetId;

  @Column(nullable = false, updatable = false)
  @org.hibernate.annotations.CreationTimestamp
  private java.time.LocalDateTime createdAt;

  public enum FavoriteType {
    INGREDIENT,
    RECIPE
  }
}
