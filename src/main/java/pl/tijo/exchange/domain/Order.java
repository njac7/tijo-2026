package pl.tijo.exchange.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Trader trader;

    @ManyToOne(optional = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    @Column(precision = 19, scale = 2)
    private BigDecimal limitPrice;

    private long quantity;

    private long remainingQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @Column(nullable = false)
    private Instant createdAt;

    protected Order() {
    }

    public Order(Trader trader, Company company, OrderSide side, OrderType type,
                 BigDecimal limitPrice, long quantity, Instant createdAt) {
        this.trader = trader;
        this.company = company;
        this.side = side;
        this.type = type;
        this.limitPrice = limitPrice;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.createdAt = createdAt;
    }

    public void fill(long amount) {
        if (amount <= 0 || amount > remainingQuantity) {
            throw new IllegalArgumentException("Invalid fill amount " + amount);
        }
        remainingQuantity -= amount;
        status = remainingQuantity == 0 ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
    }

    public void cancel() {
        if (!status.isActive()) {
            throw new IllegalStateException("Order is not active");
        }
        status = OrderStatus.CANCELLED;
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isBuy() {
        return side == OrderSide.BUY;
    }

    public long getFilledQuantity() {
        return quantity - remainingQuantity;
    }

    public Long getId() {
        return id;
    }

    public Trader getTrader() {
        return trader;
    }

    public Company getCompany() {
        return company;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getRemainingQuantity() {
        return remainingQuantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
