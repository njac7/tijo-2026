package pl.tijo.exchange.engine;

import pl.tijo.exchange.domain.Order;

import java.math.BigDecimal;

public record Fill(Order restingOrder, long quantity, BigDecimal price) {
}
