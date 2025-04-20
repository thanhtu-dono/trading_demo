package com.example.trading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "aggregation_price")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AggregationPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symbol;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private LocalDateTime createDate;
}
