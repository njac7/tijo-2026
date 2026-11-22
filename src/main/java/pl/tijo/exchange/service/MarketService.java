package pl.tijo.exchange.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tijo.exchange.domain.Company;
import pl.tijo.exchange.domain.Holding;
import pl.tijo.exchange.domain.Order;
import pl.tijo.exchange.domain.OrderSide;
import pl.tijo.exchange.domain.OrderStatus;
import pl.tijo.exchange.domain.Trade;
import pl.tijo.exchange.domain.Trader;
import pl.tijo.exchange.engine.MatchingEngine;
import pl.tijo.exchange.repository.CompanyRepository;
import pl.tijo.exchange.repository.HoldingRepository;
import pl.tijo.exchange.repository.OrderRepository;
import pl.tijo.exchange.repository.TradeRepository;
import pl.tijo.exchange.repository.TraderRepository;
import pl.tijo.exchange.service.view.CompanySummary;
import pl.tijo.exchange.service.view.OrderBook;
import pl.tijo.exchange.service.view.OrderBookLevel;
import pl.tijo.exchange.service.view.Portfolio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MarketService {

    private static final EnumSet<OrderStatus> ACTIVE = EnumSet.of(OrderStatus.NEW, OrderStatus.PARTIALLY_FILLED);

    private final CompanyRepository companies;
    private final TraderRepository traders;
    private final HoldingRepository holdings;
    private final OrderRepository orders;
    private final TradeRepository trades;

    public MarketService(CompanyRepository companies, TraderRepository traders, HoldingRepository holdings,
                         OrderRepository orders, TradeRepository trades) {
        this.companies = companies;
        this.traders = traders;
        this.holdings = holdings;
        this.orders = orders;
        this.trades = trades;
    }

    public List<CompanySummary> companySummaries() {
        return companies.findAllByOrderByTickerAsc().stream()
                .map(company -> {
                    OrderBook book = orderBook(company);
                    return new CompanySummary(company.getTicker(), company.getName(), company.getLastPrice(),
                            book.bestBid(), book.bestAsk());
                })
                .toList();
    }

    public Company company(String ticker) {
        return companies.findByTicker(ticker)
                .orElseThrow(() -> new NotFoundException("Nie ma spółki " + ticker + "."));
    }

    public OrderBook orderBook(String ticker) {
        return orderBook(company(ticker));
    }

    public List<Trade> recentTrades(String ticker) {
        return trades.findTop20ByCompanyOrderByExecutedAtDescIdDesc(company(ticker));
    }

    public List<Trade> allTrades() {
        return trades.findTop100ByOrderByExecutedAtDescIdDesc();
    }

    public List<Trader> traders() {
        return traders.findAllByOrderByIdAsc();
    }

    public Trader trader(Long id) {
        return traders.findById(id).orElseThrow(() -> new NotFoundException("Nie ma inwestora nr " + id + "."));
    }

    public Portfolio portfolio(Long traderId) {
        Trader trader = trader(traderId);
        List<Holding> owned = holdings.findByTraderOrderByCompanyTickerAsc(trader).stream()
                .filter(h -> h.getQuantity() > 0)
                .toList();
        List<Order> traderOrders = orders.findByTraderOrderByCreatedAtDescIdDesc(trader);
        return new Portfolio(trader, owned, traderOrders);
    }

    private OrderBook orderBook(Company company) {
        return new OrderBook(company.getTicker(),
                levels(company, OrderSide.BUY),
                levels(company, OrderSide.SELL));
    }

    private List<OrderBookLevel> levels(Company company, OrderSide side) {
        List<Order> active = orders.findByCompanyAndSideAndStatusIn(company, side, ACTIVE).stream()
                .sorted(MatchingEngine.priority(side))
                .toList();
        List<OrderBookLevel> levels = new ArrayList<>();
        BigDecimal price = null;
        long quantity = 0;
        int count = 0;
        for (Order order : active) {
            if (price != null && price.compareTo(order.getLimitPrice()) != 0) {
                levels.add(new OrderBookLevel(price, quantity, count));
                quantity = 0;
                count = 0;
            }
            price = order.getLimitPrice();
            quantity += order.getRemainingQuantity();
            count++;
        }
        if (price != null) {
            levels.add(new OrderBookLevel(price, quantity, count));
        }
        return levels;
    }
}
