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

        sell(anna, "ALFA", 100, "51.00");
        sell(jan, "ALFA", 50, "51.50");
        sell(maria, "ALFA", 80, "52.00");
        buy(piotr, "ALFA", 60, "49.50");
        buy(maria, "ALFA", 40, "49.00");

        sell(maria, "BETA", 30, "121.00");
        sell(anna, "BETA", 20, "122.50");
        buy(jan, "BETA", 10, "119.00");

        sell(jan, "GAMA", 200, "36.00");
        sell(piotr, "GAMA", 100, "36.20");
        buy(anna, "GAMA", 150, "35.00");
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
