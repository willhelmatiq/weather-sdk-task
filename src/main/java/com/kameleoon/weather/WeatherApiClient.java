package com.kameleoon.weather;

import java.time.Duration;

/**
 * Main entry point of the SDK.
 * Provides methods to query weather data from OpenWeatherMap API.
 */
public class WeatherApiClient {
//    private final WeatherMode mode;
    private final WeatherFetcher fetcher;
    private final WeatherCache cache;
    private PollingService pollingService;

    /**
     * Creates a new instance of the API client.
     *
     * @param apiKey OpenWeather API key
     * @param mode   SDK mode (ON_DEMAND or POLLING)
     */
    public WeatherApiClient(String apiKey, WeatherMode mode) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }
        this.cache = new WeatherCache();
        this.fetcher = new WeatherFetcher(apiKey);

        if (mode == WeatherMode.POLLING) {
            this.pollingService = new PollingService(cache, fetcher, Duration.ofMinutes(2));
            this.pollingService.start();
        }
    }

    /**
     * Returns the current weather for the given city.
     * Uses cached data if available and still valid.
     *
     * @param cityName Name of the city (e.g., "London")
     * @return WeatherData object containing weather details
     */
    public WeatherData getWeather(String cityName) {
        WeatherData cached = cache.get(cityName);
        if (cached != null) {
            return cached;
        }

        try {
            WeatherData fresh = fetcher.fetchWeatherFromAPI(cityName);
            cache.put(cityName, fresh);
            return fresh;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data for " + cityName, e);
        }
    }


    /**
     * Shuts down background services (if any).
     */
    public void shutdown() {
        if (pollingService != null) {
            pollingService.stop();
        }
    }
}
