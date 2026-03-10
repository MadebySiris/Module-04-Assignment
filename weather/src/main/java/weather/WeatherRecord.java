package weather;

import java.time.LocalDate;
import java.util.Objects;

/**
 * ## WeatherRecord
 *
 * A Java Record representing a single day's weather observation.
 * Records are immutable data carriers introduced in Java 16 (JEP 395).
 *
 * ### Fields
 * | Field           | Type        | Description                        |
 * |-----------------|-------------|------------------------------------|
 * | `date`          | LocalDate   | The date of the observation        |
 * | `temperature`   | double      | Temperature in Celsius             |
 * | `humidity`      | double      | Relative humidity as a percentage  |
 * | `precipitation` | double      | Precipitation in millimetres       |
 *
 * ### Example
 * {@snippet :
 *   var record = new WeatherRecord(LocalDate.of(2023, 8, 1), 32.5, 65.0, 0.0);
 *   System.out.println(record.date());          // 2023-08-01
 *   System.out.println(record.temperature());   // 32.5
 *   System.out.println(record.isRainy());       // false
 * }
 *
 * @param date          the observation date (must not be null)
 * @param temperature   temperature in Celsius (must be between -100 and 60)
 * @param humidity      relative humidity percentage (must be 0–100)
 * @param precipitation precipitation in mm (must be non-negative)
 */
public record WeatherRecord(LocalDate date, double temperature, double humidity, double precipitation) {

    /**
     * Compact canonical constructor that validates all field values at the
     * system boundary (i.e., when data is first parsed from an external source).
     */
    public WeatherRecord {
        Objects.requireNonNull(date, "Date must not be null");
        if (temperature < -100 || temperature > 60)
            throw new IllegalArgumentException("Temperature out of range: " + temperature);
        if (humidity < 0 || humidity > 100)
            throw new IllegalArgumentException("Humidity out of range: " + humidity);
        if (precipitation < 0)
            throw new IllegalArgumentException("Precipitation cannot be negative: " + precipitation);
    }

    /**
     * ## isRainy
     *
     * Returns {@code true} when precipitation for this day is greater than zero.
     *
     * ### Example
     * {@snippet :
     *   var rainyDay = new WeatherRecord(LocalDate.of(2023, 8, 2), 35.0, 60.0, 0.2);
     *   rainyDay.isRainy(); // true
     *
     *   var dryDay = new WeatherRecord(LocalDate.of(2023, 8, 1), 32.5, 65.0, 0.0);
     *   dryDay.isRainy();   // false
     * }
     *
     * @return {@code true} if precipitation &gt; 0
     */
    public boolean isRainy() {
        return precipitation > 0;
    }

    /**
     * ## category
     *
     * Returns the weather category for this record's temperature by delegating
     * to {@link WeatherAnalyzer#categorizeWeather(double)}.
     *
     * ### Example
     * {@snippet :
     *   var record = new WeatherRecord(LocalDate.of(2023, 8, 3), 38.5, 50.0, 0.0);
     *   record.category(); // "Hot"
     * }
     *
     * @return one of {@code "Hot"}, {@code "Warm"}, or {@code "Cold"}
     */
    public String category() {
        return WeatherAnalyzer.categorizeWeather(temperature);
    }
}
