package com.example.trading.controller;

import com.example.trading.entity.TradeTransaction;
import com.example.trading.model.TradeRequest;
import com.example.trading.services.TradeTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trade")
public class TradeController {
    private final TradeTransactionService tradeService;

    public TradeController(TradeTransactionService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping()
    public ResponseEntity<String> trade(@RequestBody TradeRequest request) {
        try {
            tradeService.executeTrade(request);
            return ResponseEntity.ok("Trade executed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{userId}")
    public List<TradeTransaction> getHistory(@PathVariable Long userId) {
        return tradeService.getHistory(userId);
    }
}
