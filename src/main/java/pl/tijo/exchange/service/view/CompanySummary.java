package pl.tijo.exchange.service.view;

import java.math.BigDecimal;

public record CompanySummary(String ticker, String name, BigDecimal lastPrice, BigDecimal bestBid, BigDecimal bestAsk) {
}
