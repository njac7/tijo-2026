package pl.tijo.exchange.service.view;

import java.math.BigDecimal;

public record OrderBookLevel(BigDecimal price, long quantity, int orders) {
}
