package com.example.trading.repository;

import com.example.trading.entity.RawPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RawPriceRepository extends JpaRepository<RawPrice, Long> {
    List<RawPrice> findTop2BySymbolOrderByCreateTimeDesc(String symbol);
}
