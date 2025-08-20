package com.likelion.picklbe.domain.history.dto;

import java.time.LocalDateTime;

public record MessageDto(Long id, String role, String content, LocalDateTime createdAt) {}
