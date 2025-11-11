package com.kameleoon.weather;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Main entry point of the SDK.
 * Provides methods to query weather data from OpenWeatherMap API.
 */
public class WeatherApiClient {
    private final String apiKey;
    private final WeatherMode mode;
    private final HttpClient httpClient;
    private final WeatherCache cache;

    /**
     * Creates a new instance of the API client.
     *
     * @param apiKey OpenWeather API key
     * @param mode   SDK mode (ON_DEMAND or POLLING)
     */
    public WeatherApiClient(String apiKey, WeatherMode mode) {
        this.apiKey = apiKey;
        this.mode = mode;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(WeatherAPIConstants.DEFAULT_TIMEOUT)
                .build();
        this.cache = new WeatherCache();
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

        WeatherData freshData = fetchWeatherFromAPI(cityName);
        cache.put(cityName, freshData);
        return freshData;
    }

    /**
     * Performs a direct HTTP call to OpenWeatherMap API.
     *
     * @param cityName name of the city
     * @return parsed WeatherData object
     */
    private WeatherData fetchWeatherFromAPI(String cityName) {
        try {
            String endpoint = String.format(
                    "%s?q=%s&appid=%s&units=metric",
                    WeatherAPIConstants.BASE_URL,
                    cityName,
                    apiKey
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(WeatherAPIConstants.DEFAULT_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return WeatherData.fromJson(response.body());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch weather data: " + e.getMessage(), e);
        }
    }

}
