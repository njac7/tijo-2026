package pl.tijo.exchange.engine;

import org.springframework.stereotype.Component;
import pl.tijo.exchange.domain.Money;
import pl.tijo.exchange.domain.Order;
import pl.tijo.exchange.domain.OrderSide;
import pl.tijo.exchange.domain.OrderType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Price-time priority matching. Trades are executed at the price of the resting order.
 * Does not modify any order; the caller applies the returned fills.
 */
@Component
public class MatchingEngine {

    /**
     * @param buyBudget cash limit for a market buy order; {@code null} means no limit
     */
    public List<Fill> match(Order incoming, List<Order> restingOrders, BigDecimal buyBudget) {
        List<Order> book = restingOrders.stream()
                .filter(Order::isActive)
                .filter(o -> o.getSide() == incoming.getSide().opposite())
                .filter(o -> o.getType() == OrderType.LIMIT)
                .sorted(priority(incoming.getSide().opposite()))
                .toList();

        List<Fill> fills = new ArrayList<>();
        long remaining = incoming.getRemainingQuantity();
        BigDecimal budget = buyBudget;

        for (Order resting : book) {
            if (remaining == 0) {
                break;
            }
            if (sameTrader(resting, incoming)) {
                continue;
            }
            BigDecimal price = resting.getLimitPrice();
            if (!crosses(incoming, price)) {
                break;
            }
            long quantity = Math.min(remaining, resting.getRemainingQuantity());
            if (budget != null) {
                long affordable = budget.divide(price, 0, RoundingMode.DOWN).longValue();
                quantity = Math.min(quantity, affordable);
                if (quantity == 0) {
                    break;
                }
                budget = budget.subtract(Money.times(price, quantity));
            }
            fills.add(new Fill(resting, quantity, price));
            remaining -= quantity;
        }
        return fills;
    }

    public static Comparator<Order> priority(OrderSide side) {
        Comparator<Order> byPrice = Comparator.comparing(Order::getLimitPrice);
        if (side == OrderSide.BUY) {
            byPrice = byPrice.reversed();
        }
        return byPrice
                .thenComparing(Order::getCreatedAt)
                .thenComparing(Order::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private boolean sameTrader(Order a, Order b) {
        if (a.getTrader() == b.getTrader()) {
            return true;
        }
        Long id = a.getTrader().getId();
        return id != null && id.equals(b.getTrader().getId());
    }

    private boolean crosses(Order incoming, BigDecimal restingPrice) {
        if (incoming.getType() == OrderType.MARKET) {
            return true;
        }
        int cmp = restingPrice.compareTo(incoming.getLimitPrice());
        return incoming.isBuy() ? cmp <= 0 : cmp >= 0;
    }
}
