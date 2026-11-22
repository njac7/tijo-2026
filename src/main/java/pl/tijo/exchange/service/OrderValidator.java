package pl.tijo.exchange.service;

import org.springframework.stereotype.Component;
import pl.tijo.exchange.domain.OrderType;

import java.math.BigDecimal;

@Component
public class OrderValidator {

    public static final long MAX_QUANTITY = 1_000_000;
    public static final BigDecimal MAX_PRICE = new BigDecimal("1000000");

    public void validate(PlaceOrderCommand command) {
        if (command.traderId() == null) {
            throw new ExchangeException("Nie wybrano inwestora.");
        }
        if (command.ticker() == null || command.ticker().isBlank()) {
            throw new ExchangeException("Nie wybrano spółki.");
        }
        if (command.side() == null) {
            throw new ExchangeException("Wybierz kierunek zlecenia: kupno albo sprzedaż.");
        }
        if (command.type() == null) {
            throw new ExchangeException("Wybierz typ zlecenia.");
        }
        validateQuantity(command.quantity());
        if (command.type() == OrderType.LIMIT) {
            validateLimitPrice(command.limitPrice());
        }
    }

    private void validateQuantity(Long quantity) {
        if (quantity == null) {
            throw new ExchangeException("Podaj liczbę akcji.");
        }
        if (quantity <= 0) {
            throw new ExchangeException("Liczba akcji musi być większa od zera.");
        }
        if (quantity > MAX_QUANTITY) {
            throw new ExchangeException("Jedno zlecenie może dotyczyć najwyżej " + MAX_QUANTITY + " akcji.");
        }
    }

    private void validateLimitPrice(BigDecimal price) {
        if (price == null) {
            throw new ExchangeException("Zlecenie z limitem wymaga podania ceny.");
        }
        if (price.signum() <= 0) {
            throw new ExchangeException("Cena musi być większa od zera.");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw new ExchangeException("Cena może mieć najwyżej 2 miejsca po przecinku (krok 0,01 zł).");
        }
        if (price.compareTo(MAX_PRICE) > 0) {
            throw new ExchangeException("Cena nie może przekraczać 1 000 000 zł.");
        }
    }
}
