package pl.tijo.exchange.service;

import pl.tijo.exchange.domain.OrderSide;
import pl.tijo.exchange.domain.OrderType;

import java.math.BigDecimal;

public record PlaceOrderCommand(
        Long traderId,
        String ticker,
        OrderSide side,
        OrderType type,
        Long quantity,
        BigDecimal limitPrice) {
}
