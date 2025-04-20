package com.example.trading.services.implement;

import com.example.trading.entity.AggregationPrice;
import com.example.trading.repository.AggregationPriceRepository;
import com.example.trading.services.AggregationPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AggregationPriceServiceImpl implements AggregationPriceService {
    private final AggregationPriceRepository aggregationPriceRepository;

    public AggregationPriceServiceImpl(AggregationPriceRepository aggregationPriceRepository) {
        this.aggregationPriceRepository = aggregationPriceRepository;
    }

    @Override
    public ResponseEntity<AggregationPrice> getLatestPrices(String symbol) {
        return aggregationPriceRepository.findTopBySymbolOrderByCreateDate(symbol.toUpperCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
