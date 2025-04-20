package com.example.trading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "raw_price")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RawPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String source;
    private String symbol;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private LocalDateTime createTime;
}
