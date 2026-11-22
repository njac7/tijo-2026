package pl.tijo.exchange.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class Trader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cash;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal reservedCash = Money.ZERO;

    protected Trader() {
    }

    public Trader(String name, BigDecimal cash) {
        this.name = name;
        this.cash = Money.of(cash);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public BigDecimal getReservedCash() {
        return reservedCash;
    }

    public BigDecimal getAvailableCash() {
        return cash.subtract(reservedCash);
    }

    public void reserveCash(BigDecimal amount) {
        if (getAvailableCash().compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough available cash to reserve " + amount);
        }
        reservedCash = reservedCash.add(amount);
    }

    public void releaseCash(BigDecimal amount) {
        if (reservedCash.compareTo(amount) < 0) {
            throw new IllegalStateException("Cannot release more cash than reserved");
        }
        reservedCash = reservedCash.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        cash = cash.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (getAvailableCash().compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough available cash to withdraw " + amount);
        }
        cash = cash.subtract(amount);
    }
}
