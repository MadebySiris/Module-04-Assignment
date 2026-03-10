package weather;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ## WeatherAnalyzer
 *
 * An interface providing static analysis operations over a collection of
 * {@link WeatherRecord} instances. All processing is performed using
 * lambdas and the {@link java.util.stream.Stream} API, with no mutable state.
 *
 * ### Key Operations
 * - Average temperature for a given month
 * - Filter days above a temperature threshold
 * - Count rainy days
 * - Classify temperature into a weather category (enhanced switch + pattern matching)
 *
 * ### Example
 * {@snippet :
 *   var records = WeatherParser.parseCSV("weatherdata.csv");
 *
 *   double avg   = WeatherAnalyzer.averageTemperatureForMonth(records, 8);
 *   long   rainy = WeatherAnalyzer.countRainyDays(records);
 *   String cat   = WeatherAnalyzer.categorizeWeather(avg);
 *
 *   System.out.printf("August average: %.1f°C (%s)%n", avg, cat);
 *   System.out.println("Rainy days: " + rainy);
 * }
 */
public interface WeatherAnalyzer {

    /**
     * ## averageTemperatureForMonth
     *
     * Calculates the arithmetic mean temperature for all records whose
     * {@link WeatherRecord#date()} falls in the specified month (1–12).
     *
     * Uses a lambda predicate and {@code mapToDouble} to build a
     * {@link java.util.OptionalDouble} average in a single stream pipeline.
     *
     * ### Example
     * {@snippet :
     *   double augustAvg = WeatherAnalyzer.averageTemperatureForMonth(records, 8);
     *   System.out.printf("August average temperature: %.2f°C%n", augustAvg);
     * }
     *
     * @param records the list of {@link WeatherRecord} to analyse
     * @param month   month number (1 = January … 12 = December)
     * @return the average temperature, or {@code 0.0} if no records match
     */
    static double averageTemperatureForMonth(List<WeatherRecord> records, int month) {
        return records.stream()
                .filter(r -> r.date().getMonthValue() == month)
                .mapToDouble(WeatherRecord::temperature)
                .average()
                .orElse(0.0);
    }

    /**
     * ## daysAboveTemperature
     *
     * Returns every {@link WeatherRecord} whose temperature strictly exceeds
     * the given threshold, using a stream filter backed by a lambda.
     *
     * ### Example
     * {@snippet :
     *   var hotDays = WeatherAnalyzer.daysAboveTemperature(records, 30.0);
     *   System.out.println("Days above 30°C: " + hotDays.size());
     *   hotDays.forEach(r -> System.out.println(r.date() + " — " + r.temperature() + "°C"));
     * }
     *
     * @param records   the list of {@link WeatherRecord} to filter
     * @param threshold temperature threshold in Celsius (exclusive)
     * @return a new list containing only matching records
     */
    static List<WeatherRecord> daysAboveTemperature(List<WeatherRecord> records, double threshold) {
        return records.stream()
                .filter(r -> r.temperature() > threshold)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * ## countRainyDays
     *
     * Counts the number of records where {@link WeatherRecord#isRainy()} is
     * {@code true} (i.e. precipitation &gt; 0 mm).
     *
     * Uses a method reference as the stream predicate.
     *
     * ### Example
     * {@snippet :
     *   long rainy = WeatherAnalyzer.countRainyDays(records);
     *   System.out.println("Total rainy days: " + rainy);
     * }
     *
     * @param records the list of {@link WeatherRecord} to examine
     * @return the count of rainy days
     */
    static long countRainyDays(List<WeatherRecord> records) {
        return records.stream()
                .filter(WeatherRecord::isRainy)
                .count();
    }

    /**
     * ## categorizeWeather
     *
     * Classifies a temperature value into a human-readable weather category
     * using **pattern matching for {@code switch}** (Java 21, JEP 441) with
     * **guarded patterns** (`when` clause).
     *
     * | Range            | Category |
     * |------------------|----------|
     * | temp ≥ 30 °C     | Hot      |
     * | 20 ≤ temp &lt; 30 °C | Warm     |
     * | temp &lt; 20 °C  | Cold     |
     *
     * The temperature is boxed to {@code Double} so the switch can use
     * type patterns — a modern Java 21 language feature.
     *
     * ### Example
     * {@snippet :
     *   String cat = WeatherAnalyzer.categorizeWeather(32.5);
     *   System.out.println(cat); // Hot
     *
     *   System.out.println(WeatherAnalyzer.categorizeWeather(25.0)); // Warm
     *   System.out.println(WeatherAnalyzer.categorizeWeather(15.0)); // Cold
     * }
     *
     * @param temperature temperature in Celsius
     * @return {@code "Hot"}, {@code "Warm"}, or {@code "Cold"}
     */
    static String categorizeWeather(double temperature) {
        // Box the primitive so pattern matching for switch can work with type patterns
        Double temp = temperature;
        return switch (temp) {
            case Double t when t >= 30.0 -> "Hot";
            case Double t when t >= 20.0 -> "Warm";
            default                      -> "Cold";
        };
    }
}
