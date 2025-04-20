package com.example.trading.services;

import com.example.trading.entity.AggregationPrice;
import org.springframework.http.ResponseEntity;

public interface AggregationPriceService {
    ResponseEntity<AggregationPrice> getLatestPrices(String symbol);
}
