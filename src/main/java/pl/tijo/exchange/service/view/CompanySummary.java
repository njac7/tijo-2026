package pl.tijo.exchange.service.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

public record CompanySummary(
        String ticker,
        String name,
        BigDecimal referencePrice,
        BigDecimal lastPrice,
        BigDecimal bestBid,
        BigDecimal bestAsk,
        long volume,
        List<BigDecimal> priceHistory) {

    public BigDecimal change() {
        return lastPrice.subtract(referencePrice);
    }

    public BigDecimal changePercent() {
        return change().multiply(BigDecimal.valueOf(100)).divide(referencePrice, 2, RoundingMode.HALF_UP);
    }

    public String trend() {
        return switch (change().signum()) {
            case 1 -> "up";
            case -1 -> "down";
            default -> "flat";
        };
    }

    public BigDecimal high() {
        return priceHistory.stream().max(Comparator.naturalOrder()).orElse(lastPrice);
    }

    public BigDecimal low() {
        return priceHistory.stream().min(Comparator.naturalOrder()).orElse(lastPrice);
    }
}
