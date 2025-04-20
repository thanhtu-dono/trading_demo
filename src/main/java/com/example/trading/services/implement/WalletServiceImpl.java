package com.example.trading.services.implement;

import com.example.trading.entity.Wallet;
import com.example.trading.repository.WalletRepository;
import com.example.trading.services.WalletService;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepo;

    public WalletServiceImpl(WalletRepository walletRepo) {
        this.walletRepo = walletRepo;
    }

    @Override
    public Wallet getWallet(Long userId) {
        return walletRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
