package pl.tijo.exchange.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.tijo.exchange.domain.Order;
import pl.tijo.exchange.domain.OrderSide;
import pl.tijo.exchange.domain.OrderStatus;
import pl.tijo.exchange.domain.OrderType;
import pl.tijo.exchange.domain.Trader;
import pl.tijo.exchange.service.ExchangeException;
import pl.tijo.exchange.service.MarketService;
import pl.tijo.exchange.service.NotFoundException;
import pl.tijo.exchange.service.OrderService;
import pl.tijo.exchange.service.PlaceOrderCommand;
import pl.tijo.exchange.service.view.CompanySummary;

import java.util.List;

@Controller
public class PageController {

    static final String TRADER_SESSION_KEY = "traderId";

    private final MarketService market;
    private final OrderService orderService;

    public PageController(MarketService market, OrderService orderService) {
        this.market = market;
        this.orderService = orderService;
    }

    @ModelAttribute("traders")
    public List<Trader> traders() {
        return market.traders();
    }

    @ModelAttribute("currentTrader")
    public Trader currentTrader(HttpSession session) {
        Long id = currentTraderId(session);
        return id == null ? null : market.trader(id);
    }

    @ModelAttribute("tape")
    public List<CompanySummary> tape() {
        return market.companySummaries();
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/trader")
    public String switchTrader(@RequestParam Long traderId, HttpSession session,
                               @RequestHeader(value = "Referer", required = false) String referer) {
        market.trader(traderId);
        session.setAttribute(TRADER_SESSION_KEY, traderId);
        return "redirect:" + (referer != null ? referer : "/");
    }

    @GetMapping("/companies/{ticker}")
    public String company(@PathVariable String ticker, HttpSession session, Model model) {
        model.addAttribute("summary", market.companySummary(ticker));
        model.addAttribute("book", market.orderBook(ticker));
        model.addAttribute("availableShares", market.availableShares(currentTraderId(session), ticker));
        model.addAttribute("trades", market.recentTrades(ticker));
        if (!model.containsAttribute("orderForm")) {
            OrderForm form = new OrderForm();
            form.setTicker(ticker);
            model.addAttribute("orderForm", form);
        }
        model.addAttribute("sides", OrderSide.values());
        model.addAttribute("types", OrderType.values());
        return "company";
    }

    @PostMapping("/orders")
    public String placeOrder(@ModelAttribute OrderForm form, BindingResult binding, HttpSession session,
                             RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("error", "Niepoprawny format liczby. Ilość to liczba całkowita, "
                    + "cena to liczba z kropką, np. 50.25.");
            return "redirect:/companies/" + form.getTicker();
        }
        try {
            Order order = orderService.placeOrder(new PlaceOrderCommand(currentTraderId(session), form.getTicker(),
                    form.getSide(), form.getType(), form.getQuantity(), form.getLimitPrice()));
            redirect.addFlashAttribute("success", describe(order));
        } catch (ExchangeException | NotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addFlashAttribute("orderForm", form);
        }
        return "redirect:/companies/" + form.getTicker();
    }

    @GetMapping("/portfolio")
    public String portfolio(HttpSession session, Model model) {
        model.addAttribute("portfolio", market.portfolio(currentTraderId(session)));
        return "portfolio";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            orderService.cancelOrder(id, currentTraderId(session));
            redirect.addFlashAttribute("success", "Zlecenie nr " + id + " zostało anulowane, środki odblokowane.");
        } catch (ExchangeException | NotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portfolio";
    }

    @GetMapping("/trades")
    public String trades(Model model) {
        model.addAttribute("trades", market.allTrades());
        return "trades";
    }

    private Long currentTraderId(HttpSession session) {
        Object id = session.getAttribute(TRADER_SESSION_KEY);
        if (id instanceof Long traderId) {
            return traderId;
        }
        List<Trader> all = market.traders();
        if (all.isEmpty()) {
            return null;
        }
        Long first = all.getFirst().getId();
        session.setAttribute(TRADER_SESSION_KEY, first);
        return first;
    }

    private String describe(Order order) {
        String base = "Zlecenie nr " + order.getId() + " (" + order.getSide().getLabel().toLowerCase() + " "
                + order.getQuantity() + " szt. " + order.getCompany().getTicker() + "): ";
        long filled = order.getFilledQuantity();
        if (order.getStatus() == OrderStatus.FILLED) {
            return base + "wykonane w całości.";
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return filled == 0
                    ? base + "brak ofert po drugiej stronie, zlecenie PKC anulowane."
                    : base + "wykonano " + filled + " szt., reszta zlecenia PKC anulowana.";
        }
        return filled == 0
                ? base + "czeka w arkuszu zleceń."
                : base + "wykonano " + filled + " szt., reszta czeka w arkuszu.";
    }
}
