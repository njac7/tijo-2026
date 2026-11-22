package pl.tijo.exchange.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Company company;

    @ManyToOne(optional = false)
    private Order buyOrder;

    @ManyToOne(optional = false)
    private Order sellOrder;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    private long quantity;

    @Column(nullable = false)
    private Instant executedAt;

    protected Trade() {
    }

    public Trade(Company company, Order buyOrder, Order sellOrder, BigDecimal price, long quantity, Instant executedAt) {
        this.company = company;
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
        this.price = price;
        this.quantity = quantity;
        this.executedAt = executedAt;
    }

    public BigDecimal getValue() {
        return Money.times(price, quantity);
    }

    public Long getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public Order getBuyOrder() {
        return buyOrder;
    }

    public Order getSellOrder() {
        return sellOrder;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
