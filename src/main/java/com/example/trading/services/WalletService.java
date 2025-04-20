package com.example.trading.services;

import com.example.trading.entity.Wallet;

public interface WalletService {
    Wallet getWallet(Long userId);
}
