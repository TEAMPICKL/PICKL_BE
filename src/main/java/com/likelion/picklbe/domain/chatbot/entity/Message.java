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
public class Message extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long conversationId;

  @Enumerated(EnumType.STRING)
  private MessageRole role;

  @Column(columnDefinition = "TEXT")
  private String content;
}
