package com.example.trading.services;

import com.example.trading.entity.TradeTransaction;
import com.example.trading.model.TradeRequest;

import java.util.List;

public interface TradeTransactionService {
    void executeTrade(TradeRequest request);
    List<TradeTransaction> getHistory(Long userId);
}
