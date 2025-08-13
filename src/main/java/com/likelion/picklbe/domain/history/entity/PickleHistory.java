package com.likelion.picklbe.domain.history.entity;

import jakarta.persistence.*;

import com.likelion.picklbe.domain.user.entity.User;

import lombok.*;

@Entity
@Table(
    name = "pickle_history",
    indexes = {@Index(name = "idx_ph_user", columnList = "user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickleHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Action action; // EX: SEARCH, VIEW_INGREDIENT, VIEW_RECIPE, LIKE, ...

  public enum Action {
    SEARCH,
    VIEW_INGREDIENT,
    VIEW_RECIPE,
    LIKE
  }
}
