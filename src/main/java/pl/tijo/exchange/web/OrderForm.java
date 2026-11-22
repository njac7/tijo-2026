package pl.tijo.exchange.web;

import pl.tijo.exchange.domain.OrderSide;
import pl.tijo.exchange.domain.OrderType;

import java.math.BigDecimal;

public class OrderForm {

    private String ticker;
    private OrderSide side = OrderSide.BUY;
    private OrderType type = OrderType.LIMIT;
    private Long quantity;
    private BigDecimal limitPrice;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(BigDecimal limitPrice) {
        this.limitPrice = limitPrice;
    }
}
