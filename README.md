# 🌦️ Weather SDK (Java)

A lightweight, thread-safe Java SDK for accessing the [OpenWeatherMap API](https://openweathermap.org/api).  
Provides automatic caching, and configurable background polling.

---
## Key Features

- **LRU + TTL cache** – the SDK maintains up to 10 most recently used city entries by default.  
  Cached items automatically expire after a configurable time (default: 10 minutes).  
  The cache is implemented using a combination of `ConcurrentHashMap`,  
  a custom `DoublyLinkedList`, and `ReentrantReadWriteLock` for thread safety and O(1) operations.

- **Polling service** – in `POLLING` mode, the SDK runs a background daemon thread  
  that periodically refreshes weather data for all cached cities,  
  ensuring near-zero latency for subsequent requests.

- **Centralized client registry** – `WeatherClientRegistry` guarantees that only one SDK client instance  
  can exist per API key, preventing duplicate connections and resource overhead.

- **Minimal dependencies** – only [Gson](https://github.com/google/gson) (`com.google.code.gson:gson:2.13.2`)

- **Local Maven publishing** – the SDK can be built and published locally via Gradle using  
  the `maven-publish` plugin, allowing developers to add it as a dependency in other projects:
  ```groovy
  repositories { mavenLocal() }
  dependencies { implementation 'com.kameleoon:weather-sdk:1.0.0' }
  ```
---
## Quick Start

### 1. Installation

Add the Weather SDK to your project using your preferred build system.

**Gradle:**
```groovy
dependencies {
    implementation 'com.kameleoon:weather-sdk:1.0.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>com.kameleoon</groupId>
    <artifactId>weather-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

### 2. Example Usage

```java
import com.kameleoon.weather.*;

public class Main {
    public static void main(String[] args) {
        WeatherApiClient client = WeatherClientRegistry.getClient("YOUR_API_KEY", WeatherMode.ON_DEMAND);

        try {
            WeatherData data = client.getWeather("London");
            System.out.println(data.toJson());
        } catch (WeatherSdkException e) {
            e.printStackTrace();
        } finally {
            client.shutdown();
        }
    }
}
```
---
## Configuration

You can customize SDK behavior using `WeatherConfig`:

```java
import java.time.Duration;
import java.util.logging.Level;

WeatherConfig config = new WeatherConfig.Builder()
        .cacheSize(20)
        .cacheTtlSeconds(300)
        .apiTimeout(Duration.ofSeconds(5))
        .pollingInterval(Duration.ofMinutes(1))
        .logLevel(Level.INFO)
        .build();

WeatherApiClient client =
        WeatherClientRegistry.getClient("YOUR_API_KEY", WeatherMode.POLLING, config);
```

### Default configuration:

| Setting | Default | Description |
|----------|----------|--------------|
| `cacheSize` | 10 | Maximum number of cities stored |
| `cacheTtlSeconds` | 600 | Cache lifetime (10 minutes) |
| `apiTimeout` | 10s | HTTP request timeout |
| `pollingInterval` | 2 min | Interval for background updates |
| `logLevel` | WARNING | Default logging level |


## Architecture Overview

| Component | Responsibility |
|------------|----------------|
| WeatherApiClient | Main public API for SDK users |
| WeatherFetcher | Performs HTTP requests to OpenWeather API |
| WeatherCache | Thread-safe LRU cache with TTL expiration |
| PollingService | Background updater for cached cities |
| WeatherClientRegistry | Prevents duplicate clients per API key |
| WeatherConfig | Centralizes all configuration options |
| WeatherData | Immutable weather data model representing weather JSON |


## Example Output

```json
{
  "weather": {
    "main": "Clouds",
    "description": "scattered clouds"
  },
  "temperature": {
    "temp": 12.4,
    "feels_like": 11.0
  },
  "visibility": 10000,
  "wind": {
    "speed": 1.38
  },
  "datetime": 1675744800,
  "sys": {
    "sunrise": 1675751262,
    "sunset": 1675787560
  },
  "timezone": 3600,
  "name": "London"
}
```