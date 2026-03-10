# Weather Data Analyzer

A command-line Java application that parses a CSV weather dataset and produces statistical analyses and categorised reports. The project is a showcase of modern Java features introduced between Java 15 and Java 21.

---

## Features

| Feature | Description |
|---|---|
| Parse CSV | Reads `weatherdata.csv` (Date, Temperature, Humidity, Precipitation) |
| Average temperature | Calculates the mean temperature for any given month |
| Days above threshold | Lists days where temperature exceeds a configurable value |
| Rainy day count | Counts days with precipitation > 0 mm |
| Weather categorisation | Classifies each day as **Hot**, **Warm**, or **Cold** |
| Concurrent reports | Generates monthly reports in parallel using virtual threads |

---

## Modern Java Features Used

| Feature | Java Version | Where Used |
|---|---|---|
| **Records** | Java 16 (JEP 395) | `WeatherRecord.java` — immutable data carrier |
| **Text Blocks** | Java 15 (JEP 378) | `WeatherReporter.java` — all report templates |
| **`String.stripIndent()`** | Java 15 | `WeatherReporter#generateSummary` |
| **Enhanced switch expressions** | Java 14 (JEP 361) | `WeatherAnalyzer#categorizeWeather` |
| **Pattern Matching for `switch`** | Java 21 (JEP 441) | guarded `case Double t when ...` |
| **Virtual Threads** | Java 21 (JEP 444) | `Main.java` — concurrent month reports |
| **`{@snippet}` Javadoc tags** | Java 18 (JEP 413) | All classes and methods |
| **Lambdas & Streams** | Java 8+ | `WeatherAnalyzer`, `WeatherReporter` |
| **`Stream.toList()`** | Java 16 | Various stream pipelines |
| **Private interface methods** | Java 9 | `WeatherParser#parseLine` |

---

## Design Constraints

The project deliberately uses **no explicit classes** (other than `Main`, which is required as the JVM entry point). All domain logic lives in:

- **Records** — `WeatherRecord` (immutable data)
- **Interfaces with static/private methods** — `WeatherParser`, `WeatherAnalyzer`, `WeatherReporter`
- **Functional constructs** — lambdas, method references, stream pipelines

---

## Project Structure

```
weather/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── weather/
        │       ├── Main.java              # Entry point (virtual threads)
        │       ├── WeatherRecord.java     # Java Record (data carrier)
        │       ├── WeatherParser.java     # Interface — CSV parsing
        │       ├── WeatherAnalyzer.java   # Interface — data analysis
        │       └── WeatherReporter.java   # Interface — text-block reports
        └── resources/
            └── weatherdata.csv            # Sample weather dataset
```

---

## Prerequisites

| Tool | Minimum Version |
|---|---|
| JDK | **21** (LTS) |
| Apache Maven | 3.8+ |

Verify your Java version:
```bash
java -version
```

---

## Building & Running

```bash
# Compile
mvn compile

# Run
mvn exec:java

# Compile and run in one step
mvn compile exec:java
```

Expected output includes a detailed per-record table, three monthly summary reports (July, August, September), and an overall dataset summary.

---

## CSV Format

Place your data file at `src/main/resources/weatherdata.csv`. The first row must be the header:

```
Date,Temperature,Humidity,Precipitation
2023-08-01,32.5,65,0.0
2023-08-02,35.0,60,0.2
```

| Column | Type | Description |
|---|---|---|
| `Date` | `YYYY-MM-DD` | Observation date |
| `Temperature` | `double` | Degrees Celsius |
| `Humidity` | `double` | Percentage (0–100) |
| `Precipitation` | `double` | Millimetres |

---

## Sample Output

```
╔════════════════════════════════════════════╗
         Weather Data Analyzer v1.0
       Modern Java Features Showcase
╚════════════════════════════════════════════╝

Loaded 30 weather records.

┌──────────────────────────────────────────────────────────────┐
  Date         Temp(°C)  Humidity(%)  Precip(mm)  Category
├──────────────────────────────────────────────────────────────┤
  2023-07-01      28.0        70.0         0.0  Warm
  2023-07-02      30.5        65.0         0.0  Hot
  ...

╔══════════════════════════════════════╗
       Weather Analysis Report — AUGUST
╚══════════════════════════════════════╝
  Avg Temperature : 33.15°C
  Category        : Hot
  Days Above 30°C : 7
  Rainy Days      : 2
════════════════════════════════════════
```

---

## Architecture Highlights

### Pattern Matching for `switch` (Java 21)
```java
Double temp = temperature;
return switch (temp) {
    case Double t when t >= 30.0 -> "Hot";
    case Double t when t >= 20.0 -> "Warm";
    default                      -> "Cold";
};
```

### Text Blocks (Java 15)
```java
return """
        ╔══════════════════════════════════════╗
               Weather Analysis Report — %s
        ╚══════════════════════════════════════╝
          Avg Temperature : %.2f°C
        """.formatted(monthName, avgTemp);
```

### Virtual Threads (Java 21)
```java
Thread.ofVirtual()
      .name("report-thread-" + month)
      .start(() -> reports.put(month, WeatherReporter.generateMonthlyReport(records, month)));
```

### Java Records
```java
public record WeatherRecord(
    LocalDate date,
    double temperature,
    double humidity,
    double precipitation
) { ... }
```

---

## License

This project was created for educational purposes as part of an Advanced Programming assignment.
