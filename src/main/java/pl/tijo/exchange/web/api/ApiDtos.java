package pl.tijo.exchange.web.api;

import pl.tijo.exchange.domain.Holding;
import pl.tijo.exchange.domain.Order;
import pl.tijo.exchange.domain.OrderSide;
import pl.tijo.exchange.domain.OrderStatus;
import pl.tijo.exchange.domain.OrderType;
import pl.tijo.exchange.domain.Trade;
import pl.tijo.exchange.domain.Trader;
import pl.tijo.exchange.service.view.Portfolio;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record TraderDto(Long id, String name, BigDecimal cash, BigDecimal reservedCash, BigDecimal availableCash) {
        static TraderDto from(Trader t) {
            return new TraderDto(t.getId(), t.getName(), t.getCash(), t.getReservedCash(), t.getAvailableCash());
        }
    }

    public record HoldingDto(String ticker, long quantity, long reservedQuantity, long availableQuantity) {
        static HoldingDto from(Holding h) {
            return new HoldingDto(h.getCompany().getTicker(), h.getQuantity(), h.getReservedQuantity(),
                    h.getAvailableQuantity());
        }
    }

    public record OrderDto(Long id, Long traderId, String ticker, OrderSide side, OrderType type,
                           BigDecimal limitPrice, long quantity, long filledQuantity, long remainingQuantity,
                           OrderStatus status, Instant createdAt) {
        static OrderDto from(Order o) {
            return new OrderDto(o.getId(), o.getTrader().getId(), o.getCompany().getTicker(), o.getSide(),
                    o.getType(), o.getLimitPrice(), o.getQuantity(), o.getFilledQuantity(),
                    o.getRemainingQuantity(), o.getStatus(), o.getCreatedAt());
        }
    }

    public record TradeDto(Long id, String ticker, BigDecimal price, long quantity, Long buyOrderId,
                           Long sellOrderId, Instant executedAt) {
        static TradeDto from(Trade t) {
            return new TradeDto(t.getId(), t.getCompany().getTicker(), t.getPrice(), t.getQuantity(),
                    t.getBuyOrder().getId(), t.getSellOrder().getId(), t.getExecutedAt());
        }
    }

    public record PortfolioDto(TraderDto trader, List<HoldingDto> holdings, List<OrderDto> orders,
                               BigDecimal sharesValue, BigDecimal totalValue) {
        static PortfolioDto from(Portfolio p) {
            return new PortfolioDto(TraderDto.from(p.trader()),
                    p.holdings().stream().map(HoldingDto::from).toList(),
                    p.orders().stream().map(OrderDto::from).toList(),
                    p.sharesValue(), p.totalValue());
        }
    }
}
