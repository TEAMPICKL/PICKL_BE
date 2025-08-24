package com.likelion.picklbe.domain.marketprice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.likelion.picklbe.domain.marketprice.entity.MarketPrice;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

  Optional<MarketPrice> findByProductNameAndUnit(String productName, String unit);

  List<MarketPrice> findAllByProductNameIn(List<String> productNames);
}
