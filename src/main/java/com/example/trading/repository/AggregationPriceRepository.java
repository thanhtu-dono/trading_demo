package com.example.trading.repository;

import com.example.trading.entity.AggregationPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AggregationPriceRepository extends JpaRepository<AggregationPrice, Long> {
    Optional<AggregationPrice> findTopBySymbolOrderByCreateDate(String symbol);
}
