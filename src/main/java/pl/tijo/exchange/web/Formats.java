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

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final Clock clock;

    public Formats(Clock clock) {
        this.clock = clock;
    }

    public String money(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.of("pl", "PL"));
        symbols.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0.00", symbols).format(value);
    }

    public String time(Instant instant) {
        return instant == null ? "—" : TIME.format(instant.atZone(clock.getZone()));
    }
}
