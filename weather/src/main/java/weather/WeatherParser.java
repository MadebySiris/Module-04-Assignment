package weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ## WeatherParser
 *
 * An interface providing static utilities for parsing weather data from CSV files.
 * No instances are ever created — all members are {@code static}.
 *
 * The expected CSV format is:
 * ```
 * Date,Temperature,Humidity,Precipitation
 * 2023-08-01,32.5,65,0.0
 * ```
 *
 * ### Example
 * {@snippet :
 *   List<WeatherRecord> records = WeatherParser.parseCSV("weatherdata.csv");
 *   System.out.println("Loaded " + records.size() + " records.");
 * }
 */
public interface WeatherParser {

    /**
     * ## parseCSV
     *
     * Loads and parses a CSV file from the application classpath.
     * The first row (header) is automatically skipped.
     * Each subsequent non-blank line is converted to a {@link WeatherRecord}.
     *
     * This method uses lambdas and {@link java.util.stream.Stream} operations
     * to process every CSV line functionally, with no explicit looping.
     *
     * ### Example
     * {@snippet :
     *   // Place weatherdata.csv in src/main/resources/ and call:
     *   List<WeatherRecord> data = WeatherParser.parseCSV("weatherdata.csv");
     *   data.stream()
     *       .filter(WeatherRecord::isRainy)
     *       .forEach(System.out::println);
     * }
     *
     * @param resourceName the classpath resource name (e.g. {@code "weatherdata.csv"})
     * @return an unmodifiable {@link List} of {@link WeatherRecord} instances
     * @throws RuntimeException if the resource cannot be found or read
     */
    static List<WeatherRecord> parseCSV(String resourceName) {
        var classLoader = Thread.currentThread().getContextClassLoader();
        try (
            var stream = Objects.requireNonNull(
                classLoader.getResourceAsStream(resourceName),
                "Resource not found on classpath: " + resourceName
            );
            var reader = new BufferedReader(new InputStreamReader(stream))
        ) {
            return reader.lines()
                    .skip(1)                          // skip header row
                    .filter(line -> !line.isBlank())  // ignore empty lines
                    .map(WeatherParser::parseLine)
                    .collect(Collectors.toUnmodifiableList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourceName, e);
        }
    }

    /**
     * Parses a single CSV line into a {@link WeatherRecord}.
     *
     * @param line one CSV data line (no header)
     * @return a new {@link WeatherRecord}
     */
    private static WeatherRecord parseLine(String line) {
        var parts = line.split(",");
        return new WeatherRecord(
                LocalDate.parse(parts[0].strip()),
                Double.parseDouble(parts[1].strip()),
                Double.parseDouble(parts[2].strip()),
                Double.parseDouble(parts[3].strip())
        );
    }
}
