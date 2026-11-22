package pl.tijo.exchange.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.tijo.exchange.service.MarketService;
import pl.tijo.exchange.service.OrderService;
import pl.tijo.exchange.service.PlaceOrderCommand;
import pl.tijo.exchange.service.view.CompanySummary;
import pl.tijo.exchange.service.view.OrderBook;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final MarketService market;
    private final OrderService orderService;

    public ApiController(MarketService market, OrderService orderService) {
        this.market = market;
        this.orderService = orderService;
    }

    @GetMapping("/companies")
    public List<CompanySummary> companies() {
        return market.companySummaries();
    }

    @GetMapping("/companies/{ticker}/orderbook")
    public OrderBook orderBook(@PathVariable String ticker) {
        return market.orderBook(ticker);
    }

    @GetMapping("/companies/{ticker}/trades")
    public List<ApiDtos.TradeDto> companyTrades(@PathVariable String ticker) {
        return market.recentTrades(ticker).stream().map(ApiDtos.TradeDto::from).toList();
    }

    @GetMapping("/trades")
    public List<ApiDtos.TradeDto> trades() {
        return market.allTrades().stream().map(ApiDtos.TradeDto::from).toList();
    }

    @GetMapping("/traders")
    public List<ApiDtos.TraderDto> traders() {
        return market.traders().stream().map(ApiDtos.TraderDto::from).toList();
    }

    @GetMapping("/traders/{id}/portfolio")
    public ApiDtos.PortfolioDto portfolio(@PathVariable Long id) {
        return ApiDtos.PortfolioDto.from(market.portfolio(id));
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.OrderDto placeOrder(@RequestBody PlaceOrderCommand command) {
        return ApiDtos.OrderDto.from(orderService.placeOrder(command));
    }

    @DeleteMapping("/orders/{id}")
    public ApiDtos.OrderDto cancelOrder(@PathVariable Long id, @RequestParam Long traderId) {
        return ApiDtos.OrderDto.from(orderService.cancelOrder(id, traderId));
    }
}
