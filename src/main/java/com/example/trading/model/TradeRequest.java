package com.example.trading.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeRequest {
    private Long userId;
    private String symbol;
    private String side;
    private BigDecimal quantity;
}
