package com.example.trading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "trade_transaction")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;
    private String symbol;
    private String side;
    private BigDecimal quantity;
    private BigDecimal price;
    private LocalDateTime createDate;

}
