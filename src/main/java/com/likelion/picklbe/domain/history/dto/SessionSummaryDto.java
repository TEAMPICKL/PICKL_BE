package com.likelion.picklbe.domain.history.dto;

import java.time.LocalDateTime;

public record SessionSummaryDto(Long id, String title, LocalDateTime modifiedAt) {}
