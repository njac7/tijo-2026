package pl.tijo.exchange.domain;

public enum OrderType {
    LIMIT("Z limitem ceny"),
    MARKET("PKC (po każdej cenie)");

    private final String label;

    OrderType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
