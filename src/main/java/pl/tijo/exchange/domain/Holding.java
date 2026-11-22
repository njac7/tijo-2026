package pl.tijo.exchange.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"trader_id", "company_id"}))
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trader_id")
    private Trader trader;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    private long quantity;

    private long reservedQuantity;

    protected Holding() {
    }

    public Holding(Trader trader, Company company, long quantity) {
        this.trader = trader;
        this.company = company;
        this.quantity = quantity;
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

    public long getQuantity() {
        return quantity;
    }

    public long getReservedQuantity() {
        return reservedQuantity;
    }

    public long getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public void reserve(long amount) {
        if (getAvailableQuantity() < amount) {
            throw new IllegalStateException("Not enough available shares to reserve " + amount);
        }
        reservedQuantity += amount;
    }

    public void release(long amount) {
        if (reservedQuantity < amount) {
            throw new IllegalStateException("Cannot release more shares than reserved");
        }
        reservedQuantity -= amount;
    }

    public void add(long amount) {
        quantity += amount;
    }

    public void removeReserved(long amount) {
        release(amount);
        quantity -= amount;
    }
}
