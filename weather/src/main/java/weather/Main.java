package weather;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;

/**
 * ## Main
 *
 * Entry point for the **Weather Data Analyzer** application.
 *
 * Demonstrates the following modern Java features:
 * - **Records** ({@link WeatherRecord}) — Java 16
 * - **Text Blocks** ({@link WeatherReporter}) — Java 15
 * - **Pattern Matching for switch** ({@link WeatherAnalyzer#categorizeWeather}) — Java 21
 * - **Enhanced switch expressions** — Java 14+
 * - **{@code Stream} + lambdas** ({@link WeatherAnalyzer}, {@link WeatherReporter})
 * - **Virtual Threads** (Java 21, JEP 444) — concurrent monthly report generation
 * - **{@code String.stripIndent}** (Java 15) — used in {@link WeatherReporter#generateSummary}
 * - **{@code @snippet}** Javadoc tags (Java 18, JEP 413)
 *
 * ### Example
 * {@snippet :
 *   // Run from the project root:
 *   // mvn compile exec:java
 * }
 */
public class Main {

    /**
     * Application entry point.
     *
     * <ol>
     *   <li>Parses {@code weatherdata.csv} from the classpath.</li>
     *   <li>Prints a full detailed log of every record.</li>
     *   <li>Uses <strong>virtual threads</strong> (Java 21) to compute monthly
     *       reports for July, August, and September concurrently.</li>
     *   <li>Prints each monthly report in calendar order.</li>
     *   <li>Prints an overall dataset summary.</li>
     * </ol>
     *
     * @param args command-line arguments (unused)
     * @throws InterruptedException if a virtual thread is interrupted
     */
    public static void main(String[] args) throws InterruptedException {

        // ── Banner (Text Block) ───────────────────────────────────────────────
        System.out.println("""
                ╔════════════════════════════════════════════╗
                         Weather Data Analyzer v1.0
                       Modern Java Features Showcase
                ╚════════════════════════════════════════════╝
                """);

        // ── Parse CSV from classpath (src/main/resources/weatherdata.csv) ──────
        List<WeatherRecord> records = WeatherParser.parseCSV("weatherdata.csv");
        System.out.println("Loaded " + records.size() + " weather records.\n");

        // ── Detailed log of every record ─────────────────────────────────────
        System.out.println(WeatherReporter.generateDetailedLog(records));
        System.out.println();

        // ── Virtual Threads (Java 21, JEP 444) ───────────────────────────────
        // Generate monthly reports concurrently; results stored in a sorted map
        // so they print in calendar order regardless of completion order.
        var months      = List.of(7, 8, 9);
        var reports     = new ConcurrentSkipListMap<Integer, String>();
        var latch       = new CountDownLatch(months.size());

        for (int month : months) {
            final int m = month;
            Thread.ofVirtual()
                  .name("report-thread-" + m)
                  .start(() -> {
                      reports.put(m, WeatherReporter.generateMonthlyReport(records, m));
                      latch.countDown();
                  });
        }

        latch.await();   // wait for all virtual threads to finish

        System.out.println("── Monthly Reports (generated with Virtual Threads) ──\n");
        reports.values().forEach(System.out::println);

        // ── Overall summary ───────────────────────────────────────────────────
        System.out.println(WeatherReporter.generateSummary(records));
    }
}
