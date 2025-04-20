package com.example.trading.services.implement;

import com.example.trading.entity.AggregationPrice;
import com.example.trading.entity.RawPrice;
import com.example.trading.repository.AggregationPriceRepository;
import com.example.trading.repository.RawPriceRepository;
import com.example.trading.services.RawPriceService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RawPriceServiceImpl implements RawPriceService {

    private final RawPriceRepository rawPriceRepository;
    private final AggregationPriceRepository aggregationPriceRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${api.url.binance}")
    private String binanceUrl;
    @Value("${api.url.houbi}")
    private String houbiUrl;

    public RawPriceServiceImpl(RawPriceRepository rawPriceRepository,
                               AggregationPriceRepository aggregationPriceRepository,
                               WebClient.Builder webClientBuilder) {
        this.rawPriceRepository = rawPriceRepository;
        this.aggregationPriceRepository = aggregationPriceRepository;
        this.webClientBuilder = webClientBuilder;
    }

    @PostConstruct
    @Scheduled(fixedRate = 10000)
    public void startFetchingPrices() {
        fetchBinancePrices();
        fetchHoubiPrices();
    }

    private void fetchBinancePrices() {
        webClientBuilder.build()
                .get()
                .uri(binanceUrl)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .filter(jsonNode -> "BTCUSDT".equals(jsonNode.get("symbol").asText()) || "ETHUSDT".equals(jsonNode.get("symbol").asText()))
                .map(this::parseBinance)
                .flatMap(this::saveAndAggregate)
                .subscribe();
    }

    private void fetchHoubiPrices() {
        webClientBuilder.build()
                .get()
                .uri(houbiUrl)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMapMany(jsonNode -> {
                    List<JsonNode> filteredTickers = StreamSupport.stream(jsonNode.get("data").spliterator(), false)
                            .filter(ticker -> "btc".equalsIgnoreCase(ticker.get("symbol").asText()) || "eth".equalsIgnoreCase(ticker.get("symbol").asText()))
                            .collect(Collectors.toList());
                    return Flux.fromIterable(filteredTickers);
                })
                .map(this::parseHoubi)
                .flatMap(this::saveAndAggregate)
                .subscribe();
    }

    private RawPrice parseBinance(JsonNode node) {
        try {
            RawPrice raw = new RawPrice();
            raw.setSource("BINANCE");
            raw.setSymbol(node.get("symbol").asText());
            raw.setBidPrice(new BigDecimal(node.get("bidPrice").asText()));
            raw.setAskPrice(new BigDecimal(node.get("askPrice").asText()));
            raw.setCreateTime(LocalDateTime.now());
            return raw;
        } catch (Exception e) {
            throw new RuntimeException("Parse error: " + e.getMessage());
        }
    }

    private RawPrice parseHoubi(JsonNode node) {
        try {
            RawPrice raw = new RawPrice();
            String symbol = node.get("symbol").asText().toUpperCase().contains("BTC") ? "BTCUSDT" : "ETHUSDT";
            BigDecimal bid = new BigDecimal(node.get("close").asText());
            BigDecimal ask = bid;
            raw.setSource("HOUBI");
            raw.setSymbol(symbol);
            raw.setBidPrice(bid);
            raw.setAskPrice(ask);
            raw.setCreateTime(LocalDateTime.now());
            return raw;
        } catch (Exception e) {
            System.err.println("Houbi parse error: " + e.getMessage());
            return null;
        }
    }

    private Mono<RawPrice> saveAndAggregate(RawPrice raw) {
        if (raw == null) return Mono.empty();
        return Mono.fromCallable(() -> rawPriceRepository.save(raw))
                .doOnSuccess(saved -> triggerAggregation(raw.getSymbol()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private void triggerAggregation(String symbol) {
        List<RawPrice> recent = rawPriceRepository.findTop2BySymbolOrderByCreateTimeDesc(symbol);
        if (recent.size() < 2) return;

        BigDecimal bestBid = recent.stream()
                .map(RawPrice::getBidPrice)
                .max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);

        BigDecimal bestAsk = recent.stream()
                .map(RawPrice::getAskPrice)
                .min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);

        AggregationPrice agg = aggregationPriceRepository.findTopBySymbolOrderByCreateDate(symbol)
                .orElse(new AggregationPrice());

        agg.setSymbol(symbol);
        agg.setBidPrice(bestBid);
        agg.setAskPrice(bestAsk);
        agg.setCreateDate(LocalDateTime.now());

        aggregationPriceRepository.save(agg);
    }
}