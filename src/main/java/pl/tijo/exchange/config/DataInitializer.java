package pl.tijo.exchange.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.tijo.exchange.domain.Company;
import pl.tijo.exchange.domain.Holding;
import pl.tijo.exchange.domain.Money;
import pl.tijo.exchange.domain.OrderSide;
import pl.tijo.exchange.domain.OrderType;
import pl.tijo.exchange.domain.Trader;
import pl.tijo.exchange.repository.CompanyRepository;
import pl.tijo.exchange.repository.HoldingRepository;
import pl.tijo.exchange.repository.TraderRepository;
import pl.tijo.exchange.service.OrderService;
import pl.tijo.exchange.service.PlaceOrderCommand;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements ApplicationRunner {

    private final CompanyRepository companies;
    private final TraderRepository traders;
    private final HoldingRepository holdings;
    private final OrderService orderService;

    public DataInitializer(CompanyRepository companies, TraderRepository traders,
                           HoldingRepository holdings, OrderService orderService) {
        this.companies = companies;
        this.traders = traders;
        this.holdings = holdings;
        this.orderService = orderService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (companies.count() > 0) {
            return;
        }
        Company alfa = companies.save(new Company("ALFA", "Alfa Energia S.A.", Money.of("50.00")));
        Company beta = companies.save(new Company("BETA", "Beta Bank S.A.", Money.of("120.00")));
        Company gama = companies.save(new Company("GAMA", "Gama Games S.A.", Money.of("35.50")));

        Trader anna = traders.save(new Trader("Anna Kowalska", Money.of("100000.00")));
        Trader jan = traders.save(new Trader("Jan Nowak", Money.of("50000.00")));
        Trader piotr = traders.save(new Trader("Piotr Wiśniewski", Money.of("20000.00")));
        Trader maria = traders.save(new Trader("Maria Zielińska", Money.of("75000.00")));

        holdings.save(new Holding(anna, alfa, 500));
        holdings.save(new Holding(anna, beta, 100));
        holdings.save(new Holding(jan, alfa, 300));
        holdings.save(new Holding(jan, gama, 1000));
        holdings.save(new Holding(piotr, gama, 400));
        holdings.save(new Holding(maria, beta, 200));
        holdings.save(new Holding(maria, alfa, 150));

        seedTradeHistory(anna, jan, piotr, maria);
        seedOrderBooks(anna, jan, piotr, maria);
    }

    private void seedTradeHistory(Trader anna, Trader jan, Trader piotr, Trader maria) {
        trade(jan, maria, "ALFA", 20, "50.20");
        trade(anna, maria, "ALFA", 15, "49.80");
        trade(jan, anna, "ALFA", 30, "50.40");
        trade(anna, maria, "ALFA", 25, "50.90");
        trade(jan, anna, "ALFA", 10, "50.60");
        trade(anna, maria, "ALFA", 40, "51.10");
        trade(jan, maria, "ALFA", 20, "50.80");

        trade(anna, jan, "BETA", 5, "119.50");
        trade(maria, piotr, "BETA", 8, "118.80");
        trade(maria, jan, "BETA", 6, "119.20");
        trade(anna, jan, "BETA", 10, "118.60");
        trade(maria, piotr, "BETA", 4, "119.00");

        trade(jan, anna, "GAMA", 50, "35.80");
        trade(piotr, maria, "GAMA", 40, "36.30");
        trade(jan, anna, "GAMA", 60, "36.10");
        trade(piotr, maria, "GAMA", 30, "36.90");
        trade(jan, anna, "GAMA", 80, "37.20");
        trade(piotr, maria, "GAMA", 25, "36.80");
    }

    private void seedOrderBooks(Trader anna, Trader jan, Trader piotr, Trader maria) {
        sell(anna, "ALFA", 100, "51.00");
        sell(jan, "ALFA", 50, "51.50");
        sell(maria, "ALFA", 80, "52.00");
        buy(piotr, "ALFA", 60, "50.50");
        buy(maria, "ALFA", 40, "50.00");

        sell(maria, "BETA", 30, "119.50");
        sell(anna, "BETA", 20, "121.00");
        buy(jan, "BETA", 10, "118.50");
        buy(piotr, "BETA", 15, "118.00");

        sell(jan, "GAMA", 200, "37.00");
        sell(piotr, "GAMA", 100, "37.40");
        buy(anna, "GAMA", 150, "36.50");
        buy(maria, "GAMA", 100, "36.00");
    }

    private void trade(Trader seller, Trader buyer, String ticker, long quantity, String price) {
        sell(seller, ticker, quantity, price);
        buy(buyer, ticker, quantity, price);
    }

    private void sell(Trader trader, String ticker, long quantity, String price) {
        place(trader, ticker, OrderSide.SELL, quantity, price);
    }

    private void buy(Trader trader, String ticker, long quantity, String price) {
        place(trader, ticker, OrderSide.BUY, quantity, price);
    }

    private void place(Trader trader, String ticker, OrderSide side, long quantity, String price) {
        orderService.placeOrder(new PlaceOrderCommand(trader.getId(), ticker, side, OrderType.LIMIT,
                quantity, new BigDecimal(price)));
    }
}
