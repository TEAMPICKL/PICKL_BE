package com.likelion.picklbe.domain.chatbot.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.chatbot.entity.UserMemory;

public interface UserMemoryRepository extends JpaRepository<UserMemory, Long> {
  List<UserMemory> findByUserIdOrderByModifiedAtDesc(Long userId, Pageable pageable);
}
