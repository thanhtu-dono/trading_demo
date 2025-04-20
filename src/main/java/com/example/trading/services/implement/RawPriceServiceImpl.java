package com.example.trading.services.implement;

import com.example.trading.entity.AggregationPrice;
import com.example.trading.entity.RawPrice;
import com.example.trading.repository.AggregationPriceRepository;
import com.example.trading.repository.RawPriceRepository;
import com.example.trading.services.RawPriceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Service
public class RawPriceServiceImpl implements RawPriceService {
    private final RawPriceRepository rawPriceRepository;
    private final AggregationPriceRepository aggregationPriceRepository;

    public RawPriceServiceImpl(RawPriceRepository rawPriceRepository, AggregationPriceRepository aggregationPriceRepository) {
        this.rawPriceRepository = rawPriceRepository;
        this.aggregationPriceRepository = aggregationPriceRepository;
    }

    @PostConstruct
    @Scheduled(fixedRate = 10000)
    public void startStreaming() {
        streamFromBinance();
        streamFromHoubi();
    }

    private void streamFromBinance() {
        List<String> streams = List.of("btcusdt@bookTicker", "ethusdt@bookTicker");
        streams.forEach(stream -> {
            String fullUrl = String.format("wss://stream.binance.com:9443/ws/%s", stream);
            ReactorNettyWebSocketClient wsClient = new ReactorNettyWebSocketClient();
            wsClient.execute(
                    URI.create(fullUrl),
                    session -> session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .map(this::parseBinance)
                            .flatMap(this::saveAndAggregate)
                            .then()
            ).subscribe();
        });
    }

    private void streamFromHoubi() {
        String url = "wss://api.huobi.pro/ws";
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        client.execute(
                URI.create(url),
                session -> session.send(
                                Flux.just(
                                        session.textMessage("{\"sub\": \"market.btcusdt.bbo\", \"id\": \"btc\"}"),
                                        session.textMessage("{\"sub\": \"market.ethusdt.bbo\", \"id\": \"eth\"}")
                                )
                        )
                        .thenMany(
                                session.receive()
                                        .map(WebSocketMessage::getPayload)
                                        .map(payload -> {
                                            try {
                                                return decompress(payload.toByteBuffer());
                                            } catch (IOException e) {
                                                throw new RuntimeException("Gzip decompress failed", e);
                                            }
                                        })
                                        .map(this::parseHoubi)
                                        .flatMap(this::saveAndAggregate)
                        )
                        .then()
        ).subscribe();
    }

    private String decompress(ByteBuffer byteBuffer) throws IOException {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);

        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
        return bf.lines().collect(Collectors.joining());
    }

    private RawPrice parseHoubi(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);

            if (node.has("ping")) return null;

            String symbol = node.get("ch").asText().toUpperCase().contains("BTC") ? "BTCUSDT" : "ETHUSDT";
            JsonNode tick = node.get("tick");

            BigDecimal bid = new BigDecimal(tick.get("bid").get(0).asText());
            BigDecimal ask = new BigDecimal(tick.get("ask").get(0).asText());

            RawPrice raw = new RawPrice();
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

    private RawPrice parseBinance(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            RawPrice raw = new RawPrice();
            raw.setSource("BINANCE");
            raw.setSymbol(node.get("s").asText());
            raw.setBidPrice(new BigDecimal(node.get("b").asText()));
            raw.setAskPrice(new BigDecimal(node.get("a").asText()));
            raw.setCreateTime(LocalDateTime.now());
            return raw;
        } catch (Exception e) {
            throw new RuntimeException("Parse error: " + e.getMessage());
        }
    }
}
