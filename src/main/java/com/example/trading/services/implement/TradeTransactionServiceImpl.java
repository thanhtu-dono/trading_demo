package com.example.trading.services.implement;

import com.example.trading.entity.AggregationPrice;
import com.example.trading.entity.TradeTransaction;
import com.example.trading.entity.Wallet;
import com.example.trading.model.TradeRequest;
import com.example.trading.repository.AggregationPriceRepository;
import com.example.trading.repository.TradeTransactionRepository;
import com.example.trading.repository.WalletRepository;
import com.example.trading.services.TradeTransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradeTransactionServiceImpl implements TradeTransactionService {
    private final AggregationPriceRepository priceRepo;
    private final WalletRepository walletRepo;
    private final TradeTransactionRepository txRepo;

    public TradeTransactionServiceImpl(AggregationPriceRepository priceRepo, WalletRepository walletRepo, TradeTransactionRepository txRepo) {
        this.priceRepo = priceRepo;
        this.walletRepo = walletRepo;
        this.txRepo = txRepo;
    }

    @Override
    public void executeTrade(TradeRequest request) {
        AggregationPrice price = priceRepo.findTopBySymbolOrderByCreateDate(request.getSymbol().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("No price available"));

        Wallet wallet = walletRepo.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BigDecimal totalPrice = pricePerUnit(request.getSide(), price).multiply(request.getQuantity());

        if ("BUY".equalsIgnoreCase(request.getSide())) {
            if (wallet.getUsdtBalance().compareTo(totalPrice) < 0)
                throw new IllegalArgumentException("Not enough USDT");

            wallet.setUsdtBalance(wallet.getUsdtBalance().subtract(totalPrice));
            if (request.getSymbol().equals("BTCUSDT")) {
                wallet.setBtcBalance(wallet.getBtcBalance().add(request.getQuantity()));
            } else {
                wallet.setEthBalance(wallet.getEthBalance().add(request.getQuantity()));
            }
        } else if ("SELL".equalsIgnoreCase(request.getSide())) {
            if (request.getSymbol().equals("BTCUSDT")) {
                if (wallet.getBtcBalance().compareTo(request.getQuantity()) < 0)
                    throw new IllegalArgumentException("Not enough BTC");
                wallet.setBtcBalance(wallet.getBtcBalance().subtract(request.getQuantity()));
                wallet.setUsdtBalance(wallet.getUsdtBalance().add(totalPrice));
            } else {
                if (wallet.getEthBalance().compareTo(request.getQuantity()) < 0)
                    throw new IllegalArgumentException("Not enough ETH");
                wallet.setEthBalance(wallet.getEthBalance().subtract(request.getQuantity()));
                wallet.setUsdtBalance(wallet.getUsdtBalance().add(totalPrice));
            }
        } else {
            throw new IllegalArgumentException("Invalid side");
        }

        walletRepo.save(wallet);

        TradeTransaction tx = new TradeTransaction();
        tx.setUserId(request.getUserId());
        tx.setSymbol(request.getSymbol());
        tx.setSide(request.getSide().toUpperCase());
        tx.setPrice(pricePerUnit(request.getSide(), price));
        tx.setQuantity(request.getQuantity());
        tx.setCreateDate(LocalDateTime.now());
        txRepo.save(tx);
    }

    @Override
    public List<TradeTransaction> getHistory(Long userId) {
        return txRepo.findByUserIdOrderByCreateDateDesc(userId);
    }

    private BigDecimal pricePerUnit(String side, AggregationPrice price) {
        return "BUY".equalsIgnoreCase(side) ? price.getAskPrice() : price.getBidPrice();
    }
}
