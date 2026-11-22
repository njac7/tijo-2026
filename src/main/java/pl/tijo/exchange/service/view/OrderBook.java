package pl.tijo.exchange.service.view;

import java.math.BigDecimal;
import java.util.List;

public record OrderBook(String ticker, List<OrderBookLevel> bids, List<OrderBookLevel> asks) {

    public BigDecimal bestBid() {
        return bids.isEmpty() ? null : bids.getFirst().price();
    }

    public BigDecimal bestAsk() {
        return asks.isEmpty() ? null : asks.getFirst().price();
    }

    public BigDecimal spread() {
        BigDecimal bid = bestBid();
        BigDecimal ask = bestAsk();
        return bid == null || ask == null ? null : ask.subtract(bid);
    }
}
