package pl.tijo.exchange.service.view;

import pl.tijo.exchange.domain.Holding;
import pl.tijo.exchange.domain.Money;
import pl.tijo.exchange.domain.Order;
import pl.tijo.exchange.domain.Trader;

import java.math.BigDecimal;
import java.util.List;

public record Portfolio(Trader trader, List<Holding> holdings, List<Order> orders) {

    public BigDecimal sharesValue() {
        return holdings.stream()
                .filter(h -> h.getCompany().getLastPrice() != null)
                .map(h -> Money.times(h.getCompany().getLastPrice(), h.getQuantity()))
                .reduce(Money.ZERO, BigDecimal::add);
    }

    public BigDecimal totalValue() {
        return trader.getCash().add(sharesValue());
    }

    public List<Order> activeOrders() {
        return orders.stream().filter(Order::isActive).toList();
    }

    public List<Order> closedOrders() {
        return orders.stream().filter(o -> !o.isActive()).toList();
    }
}
