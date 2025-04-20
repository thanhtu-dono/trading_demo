package com.example.trading.controller;

import com.example.trading.entity.AggregationPrice;
import com.example.trading.services.AggregationPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aggregation-price")
public class AggregationPriceController {
    private final AggregationPriceService aggregationPriceService;

    public AggregationPriceController(AggregationPriceService aggregationPriceService) {
        this.aggregationPriceService = aggregationPriceService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<AggregationPrice> getLatestPrice(@PathVariable String symbol) {
        return aggregationPriceService.getLatestPrices(symbol);
    }
}
