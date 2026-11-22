package pl.tijo.exchange.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tijo.exchange.domain.Company;
import pl.tijo.exchange.domain.Holding;
import pl.tijo.exchange.domain.Money;
import pl.tijo.exchange.domain.Order;
import pl.tijo.exchange.domain.OrderStatus;
import pl.tijo.exchange.domain.OrderType;
import pl.tijo.exchange.domain.Trade;
import pl.tijo.exchange.domain.Trader;
import pl.tijo.exchange.engine.Fill;
import pl.tijo.exchange.engine.MatchingEngine;
import pl.tijo.exchange.repository.CompanyRepository;
import pl.tijo.exchange.repository.HoldingRepository;
import pl.tijo.exchange.repository.OrderRepository;
import pl.tijo.exchange.repository.TradeRepository;
import pl.tijo.exchange.repository.TraderRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

@Service
public class OrderService {

    private static final EnumSet<OrderStatus> ACTIVE = EnumSet.of(OrderStatus.NEW, OrderStatus.PARTIALLY_FILLED);

    private final TraderRepository traders;
    private final CompanyRepository companies;
    private final HoldingRepository holdings;
    private final OrderRepository orders;
    private final TradeRepository trades;
    private final MatchingEngine engine;
    private final OrderValidator validator;
    private final Clock clock;

    public OrderService(TraderRepository traders, CompanyRepository companies, HoldingRepository holdings,
                        OrderRepository orders, TradeRepository trades, MatchingEngine engine,
                        OrderValidator validator, Clock clock) {
        this.traders = traders;
        this.companies = companies;
        this.holdings = holdings;
        this.orders = orders;
        this.trades = trades;
        this.engine = engine;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional
    public synchronized Order placeOrder(PlaceOrderCommand command) {
        validator.validate(command);
        Trader trader = findTrader(command.traderId());
        Company company = companies.findByTicker(command.ticker())
                .orElseThrow(() -> new NotFoundException("Nie ma spółki " + command.ticker() + "."));

        long quantity = command.quantity();
        BigDecimal limitPrice = command.type() == OrderType.LIMIT ? Money.of(command.limitPrice()) : null;
        Order order = new Order(trader, company, command.side(), command.type(), limitPrice, quantity, Instant.now(clock));

        BigDecimal buyBudget = null;
        if (order.isBuy()) {
            if (order.getType() == OrderType.LIMIT) {
                BigDecimal cost = Money.times(limitPrice, quantity);
                if (trader.getAvailableCash().compareTo(cost) < 0) {
                    throw new ExchangeException("Za mało wolnych środków: potrzeba " + cost
                            + " zł, dostępne " + trader.getAvailableCash() + " zł.");
                }
                trader.reserveCash(cost);
            } else {
                if (trader.getAvailableCash().signum() <= 0) {
                    throw new ExchangeException("Brak wolnych środków na zlecenie PKC.");
                }
                buyBudget = trader.getAvailableCash();
            }
        } else {
            Holding holding = holdings.findByTraderAndCompany(trader, company)
                    .filter(h -> h.getAvailableQuantity() >= quantity)
                    .orElseThrow(() -> new ExchangeException("Nie masz tylu wolnych akcji "
                            + company.getTicker() + " do sprzedaży."));
            holding.reserve(quantity);
        }

        orders.save(order);

        List<Order> resting = orders.findByCompanyAndSideAndStatusIn(company, order.getSide().opposite(), ACTIVE);
        for (Fill fill : engine.match(order, resting, buyBudget)) {
            settle(order, fill);
        }

        if (order.getType() == OrderType.MARKET && order.getRemainingQuantity() > 0) {
            if (!order.isBuy()) {
                holdingOf(trader, company).release(order.getRemainingQuantity());
            }
            order.cancel();
        }
        return order;
    }

    @Transactional
    public synchronized Order cancelOrder(Long orderId, Long traderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Nie ma zlecenia nr " + orderId + "."));
        if (!order.getTrader().getId().equals(traderId)) {
            throw new ExchangeException("Możesz anulować tylko własne zlecenia.");
        }
        if (!order.isActive()) {
            throw new ExchangeException("Zlecenie nr " + orderId + " jest już zakończone ("
                    + order.getStatus().getLabel().toLowerCase() + ").");
        }
        if (order.isBuy()) {
            order.getTrader().releaseCash(Money.times(order.getLimitPrice(), order.getRemainingQuantity()));
        } else {
            holdingOf(order.getTrader(), order.getCompany()).release(order.getRemainingQuantity());
        }
        order.cancel();
        return order;
    }

    private void settle(Order incoming, Fill fill) {
        Order resting = fill.restingOrder();
        Order buy = incoming.isBuy() ? incoming : resting;
        Order sell = incoming.isBuy() ? resting : incoming;
        long quantity = fill.quantity();
        BigDecimal price = fill.price();
        BigDecimal value = Money.times(price, quantity);

        Trader buyer = buy.getTrader();
        if (buy.getType() == OrderType.LIMIT) {
            buyer.releaseCash(Money.times(buy.getLimitPrice(), quantity));
        }
        buyer.withdraw(value);
        Company company = incoming.getCompany();
        holdings.findByTraderAndCompany(buyer, company)
                .orElseGet(() -> holdings.save(new Holding(buyer, company, 0)))
                .add(quantity);

        Trader seller = sell.getTrader();
        holdingOf(seller, company).removeReserved(quantity);
        seller.deposit(value);

        buy.fill(quantity);
        sell.fill(quantity);
        company.setLastPrice(price);
        trades.save(new Trade(company, buy, sell, price, quantity, Instant.now(clock)));
    }

    private Holding holdingOf(Trader trader, Company company) {
        return holdings.findByTraderAndCompany(trader, company)
                .orElseThrow(() -> new IllegalStateException("Missing holding for trader " + trader.getId()));
    }

    private Trader findTrader(Long id) {
        return traders.findById(id).orElseThrow(() -> new NotFoundException("Nie ma inwestora nr " + id + "."));
    }
}
