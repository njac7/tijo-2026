package pl.tijo.exchange.web;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component("fmt")
public class Formats {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Clock clock;

    public Formats(Clock clock) {
        this.clock = clock;
    }

    public String money(BigDecimal value) {
        return value == null ? "—" : decimal("#,##0.00").format(value);
    }

    public String signedMoney(BigDecimal value) {
        return value == null ? "—" : sign(value) + money(value.abs());
    }

    public String percent(BigDecimal value) {
        return value == null ? "—" : sign(value) + money(value.abs()) + "%";
    }

    public String quantity(long value) {
        return decimal("#,##0").format(value);
    }

    public String dateTime(Instant instant) {
        return instant == null ? "—" : DATE_TIME.format(instant.atZone(clock.getZone()));
    }

    public String time(Instant instant) {
        return instant == null ? "—" : TIME.format(instant.atZone(clock.getZone()));
    }

    private String sign(BigDecimal value) {
        return switch (value.signum()) {
            case 1 -> "+";
            case -1 -> "−";
            default -> "";
        };
    }

    private DecimalFormat decimal(String pattern) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.of("pl", "PL"));
        symbols.setGroupingSeparator(' ');
        return new DecimalFormat(pattern, symbols);
    }
}
