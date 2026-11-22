package pl.tijo.exchange.service.view;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

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

    public long maxQuantity() {
        return Stream.concat(bids.stream(), asks.stream())
                .mapToLong(OrderBookLevel::quantity)
                .max()
                .orElse(0);
    }

    public int depthPercent(OrderBookLevel level) {
        long max = maxQuantity();
        return max == 0 ? 0 : (int) Math.max(4, level.quantity() * 100 / max);
    }

    public List<OrderBookLevel> asksDescending() {
        return asks.reversed();
    }
}
