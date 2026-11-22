package pl.tijo.exchange.web;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * Builds SVG coordinates for a price line, scaled to the given viewBox size.
 */
@Component("chart")
public class PriceChart {

    private static final double PADDING = 2;

    public String line(List<BigDecimal> prices, int width, int height) {
        if (prices.isEmpty()) {
            return "";
        }
        List<BigDecimal> series = prices.size() == 1 ? List.of(prices.getFirst(), prices.getFirst()) : prices;
        double min = series.stream().min(Comparator.naturalOrder()).orElseThrow().doubleValue();
        double max = series.stream().max(Comparator.naturalOrder()).orElseThrow().doubleValue();
        double range = max - min;
        double stepX = (double) width / (series.size() - 1);
        double usableHeight = height - 2 * PADDING;

        StringJoiner points = new StringJoiner(" ");
        for (int i = 0; i < series.size(); i++) {
            double value = series.get(i).doubleValue();
            double ratio = range == 0 ? 0.5 : (value - min) / range;
            double y = PADDING + usableHeight * (1 - ratio);
            points.add(String.format(Locale.ROOT, "%.1f,%.1f", i * stepX, y));
        }
        return points.toString();
    }

    public String area(List<BigDecimal> prices, int width, int height) {
        String line = line(prices, width, height);
        return line.isEmpty() ? "" : "0," + height + " " + line + " " + width + "," + height;
    }
}
