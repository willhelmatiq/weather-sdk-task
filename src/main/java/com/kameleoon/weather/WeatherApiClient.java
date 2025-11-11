package com.kameleoon.weather;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherApiClient {
    private final String apiKey;
    private final WeatherMode mode;
    private final HttpClient httpClient;

    public WeatherApiClient(String apiKey, WeatherMode mode) {
        this.apiKey = apiKey;
        this.mode = mode;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(WeatherAPIConstants.DEFAULT_TIMEOUT)
                .build();
    }

    public WeatherData getWeather(String cityName) {
        return fetchWeatherFromAPI(cityName);
    }

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
