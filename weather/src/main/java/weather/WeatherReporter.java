package weather;

import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ## WeatherReporter
 *
 * An interface that produces human-readable reports from weather data using
 * **Text Blocks** (Java 15, JEP 378) for all multi-line string templates.
 * All methods are {@code static}; no instances are ever created.
 *
 * Text Blocks eliminate the need for explicit concatenation and escape
 * sequences, and {@link String#stripIndent()} (also Java 15) is applied
 * automatically by the compiler to align the content.
 *
 * ### Example
 * {@snippet :
 *   var records = WeatherParser.parseCSV("weatherdata.csv");
 *   System.out.println(WeatherReporter.generateMonthlyReport(records, 8));
 *   System.out.println(WeatherReporter.generateDetailedLog(records));
 *   System.out.println(WeatherReporter.generateSummary(records));
 * }
 */
public interface WeatherReporter {

    /**
     * ## generateMonthlyReport
     *
     * Builds a formatted summary report for a single calendar month using
     * a **Text Block** template and {@link String#formatted(Object...)}.
     *
     * Statistics included:
     * - Average temperature (delegated to {@link WeatherAnalyzer})
     * - Weather category (via enhanced switch pattern matching)
     * - Number of days exceeding 30 °C
     * - Number of rainy days
     *
     * ### Example
     * {@snippet :
     *   String report = WeatherReporter.generateMonthlyReport(records, 8);
     *   System.out.println(report);
     *   // ╔══════════════════════════════════════╗
     *   //        Weather Analysis Report — AUGUST
     *   // ╚══════════════════════════════════════╝
     *   //   Avg Temperature : 33.75°C
     *   //   Category        : Hot
     *   //   Days Above 30°C : 7
     *   //   Rainy Days      : 2
     * }
     *
     * @param records the full list of {@link WeatherRecord}
     * @param month   the month to report on (1–12)
     * @return a formatted, ready-to-print report string
     */
    static String generateMonthlyReport(List<WeatherRecord> records, int month) {
        double avgTemp   = WeatherAnalyzer.averageTemperatureForMonth(records, month);
        int    above30   = WeatherAnalyzer.daysAboveTemperature(records, 30.0)
                               .stream()
                               .filter(r -> r.date().getMonthValue() == month)
                               .toList()
                               .size();
        long   rainyDays = records.stream()
                               .filter(r -> r.date().getMonthValue() == month)
                               .filter(WeatherRecord::isRainy)
                               .count();
        String category  = WeatherAnalyzer.categorizeWeather(avgTemp);
        String monthName = Month.of(month).name();

        // Text Block (Java 15) — compiler calls String.stripIndent() automatically
        return """
                ╔══════════════════════════════════════╗
                       Weather Analysis Report — %s
                ╚══════════════════════════════════════╝
                  Avg Temperature : %.2f°C
                  Category        : %s
                  Days Above 30°C : %d
                  Rainy Days      : %d
                ════════════════════════════════════════
                """.formatted(monthName, avgTemp, category, above30, rainyDays);
    }

    /**
     * ## generateDetailedLog
     *
     * Produces a row-by-row table of all weather records, each annotated
     * with its temperature category. The column header is rendered using
     * a **Text Block**.
     *
     * Rows are assembled via a {@link java.util.stream.Stream} pipeline and
     * {@link Collectors#joining(CharSequence)} — no explicit loops.
     *
     * ### Example
     * {@snippet :
     *   System.out.println(WeatherReporter.generateDetailedLog(records));
     *   // Date         Temp(°C) Humidity(%) Precip(mm)  Category
     *   // ──────────────────────────────────────────────────────
     *   // 2023-08-01     32.5        65.0        0.0  Warm
     * }
     *
     * @param records the list of {@link WeatherRecord} to tabulate
     * @return a formatted table string
     */
    static String generateDetailedLog(List<WeatherRecord> records) {
        // Text Block (Java 15) for the header
        String header = """
                ┌──────────────────────────────────────────────────────────────┐
                  Date         Temp(°C)  Humidity(%)  Precip(mm)  Category
                ├──────────────────────────────────────────────────────────────┤
                """;

        String rows = records.stream()
                .map(r -> "  %-12s  %7.1f   %9.1f   %9.1f  %s".formatted(
                        r.date(),
                        r.temperature(),
                        r.humidity(),
                        r.precipitation(),
                        WeatherAnalyzer.categorizeWeather(r.temperature())))
                .collect(Collectors.joining("\n"));

        return header + rows + "\n└──────────────────────────────────────────────────────────────┘";
    }

    /**
     * ## generateSummary
     *
     * Creates a high-level summary across the entire dataset: overall average
     * temperature, total rainy days, hottest day, coldest day, and a breakdown
     * of category counts per month.
     *
     * Uses {@code String.stripIndent()} explicitly to demonstrate the Java 15
     * instance method on the dynamically built string.
     *
     * ### Example
     * {@snippet :
     *   String summary = WeatherReporter.generateSummary(records);
     *   System.out.println(summary);
     * }
     *
     * @param records the full dataset to summarise
     * @return a formatted summary report string
     */
    static String generateSummary(List<WeatherRecord> records) {
        double overallAvg = records.stream()
                .mapToDouble(WeatherRecord::temperature)
                .average()
                .orElse(0.0);

        long totalRainy = WeatherAnalyzer.countRainyDays(records);

        WeatherRecord hottest = records.stream()
                .max((a, b) -> Double.compare(a.temperature(), b.temperature()))
                .orElseThrow();

        WeatherRecord coldest = records.stream()
                .min((a, b) -> Double.compare(a.temperature(), b.temperature()))
                .orElseThrow();

        // Category distribution using groupingBy + counting
        Map<String, Long> categoryCount = records.stream()
                .collect(Collectors.groupingBy(
                        r -> WeatherAnalyzer.categorizeWeather(r.temperature()),
                        Collectors.counting()));

        // Demonstrate String.stripIndent() (Java 15) explicitly
        String raw = """
                ╔══════════════════════════════════════╗
                          Overall Dataset Summary
                ╚══════════════════════════════════════╝
                  Total Records   : %d
                  Overall Avg Temp: %.2f°C
                  Total Rainy Days: %d
                  Hottest Day     : %s (%.1f°C)
                  Coldest Day     : %s (%.1f°C)
                  Hot  Days Count : %d
                  Warm Days Count : %d
                  Cold Days Count : %d
                ════════════════════════════════════════
                """.formatted(
                records.size(),
                overallAvg,
                totalRainy,
                hottest.date(), hottest.temperature(),
                coldest.date(), coldest.temperature(),
                categoryCount.getOrDefault("Hot",  0L),
                categoryCount.getOrDefault("Warm", 0L),
                categoryCount.getOrDefault("Cold", 0L));

        // String.stripIndent() removes common leading whitespace (Java 15)
        return raw.stripIndent();
    }
}
