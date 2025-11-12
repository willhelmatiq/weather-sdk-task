package com.kameleoon.weather;

import com.kameleoon.weather.api.WeatherData;
import com.kameleoon.weather.api.WeatherFetcher;
import com.kameleoon.weather.cache.WeatherCache;
import com.kameleoon.weather.exception.WeatherAPIException;
import com.kameleoon.weather.exception.WeatherParsingException;
import com.kameleoon.weather.exception.WeatherSdkException;
import com.kameleoon.weather.polling.PollingService;

/**
 * Main entry point of the SDK.
 * Provides methods to query weather data from OpenWeatherMap API.
 * Instances of this class should be obtained via {@link WeatherClientRegistry}.
 */
public class WeatherApiClient {
    private final WeatherFetcher fetcher;
    private final WeatherCache cache;
    private final PollingService pollingService;

    /**
     * Creates a new instance of the API client.
     *
     * @param apiKey OpenWeather API key
     * @param mode   SDK mode (ON_DEMAND or POLLING)
     */
    WeatherApiClient(String apiKey, WeatherMode mode, WeatherConfig config) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }
        this.cache = new WeatherCache(config.getCacheSize(), config.getCacheTtlSeconds());
        this.fetcher = new WeatherFetcher(apiKey, config.getApiTimeout());

        if (mode == WeatherMode.POLLING) {
            this.pollingService = new PollingService(cache, fetcher, config.getPollingInterval(), config.getLogLevel());
            this.pollingService.start();
        } else {
            this.pollingService = null;
        }
    }

    /**
     * Returns the current weather for the given city.
     * Uses cached data if available and still valid.
     *
     * @param cityName Name of the city (e.g., "London")
     * @return WeatherData object containing weather details
     */
    public WeatherData getWeather(String cityName) throws WeatherSdkException {
        WeatherData cached = cache.get(cityName);
        if (cached != null) {
            return cached;
        }

        try {
            WeatherData fresh = fetcher.fetchWeatherFromAPI(cityName);
            cache.put(cityName, fresh);
            return fresh;
        } catch (WeatherAPIException | WeatherParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new WeatherSdkException("Unexpected error while fetching weather for " + cityName, e);
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
