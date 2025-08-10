package com.likelion.picklbe.domain.chatbot.entity;

import jakarta.persistence.*;

import com.likelion.picklbe.global.common.BaseTimeEntity;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = @Index(name = "idx_user_key", columnList = "userId,k", unique = true))
public class UserMemory extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  private String k;

  @Column(columnDefinition = "TEXT")
  private String v;
}
