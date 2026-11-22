package pl.tijo.exchange.domain;

public enum OrderStatus {
    NEW("Oczekuje"),
    PARTIALLY_FILLED("Częściowo wykonane"),
    FILLED("Wykonane"),
    CANCELLED("Anulowane");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isActive() {
        return this == NEW || this == PARTIALLY_FILLED;
    }
}
