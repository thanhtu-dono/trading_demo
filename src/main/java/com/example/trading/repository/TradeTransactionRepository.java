package com.example.trading.repository;

import com.example.trading.entity.TradeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {
    List<TradeTransaction> findByUserId(Long userId);
}
