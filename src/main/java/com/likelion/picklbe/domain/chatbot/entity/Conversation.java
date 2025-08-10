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
public class Conversation extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  private String title;
}
