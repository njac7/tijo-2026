package pl.tijo.exchange.domain;

public enum OrderSide {
    BUY("Kupno"),
    SELL("Sprzedaż");

    private final String label;

    OrderSide(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public OrderSide opposite() {
        return this == BUY ? SELL : BUY;
    }
}
