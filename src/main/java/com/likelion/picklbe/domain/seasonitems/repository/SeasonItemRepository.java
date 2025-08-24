package com.likelion.picklbe.domain.seasonitems.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.seasonitems.entity.SeasonItem;

public interface SeasonItemRepository extends JpaRepository<SeasonItem, Long> {

  Optional<SeasonItem> findByItemname(String itemname);
}
