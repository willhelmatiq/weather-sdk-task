package com.kameleoon.weather;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * HTTP component responsible for retrieving weather data
 * directly from the OpenWeatherMap API.
 */
public class WeatherFetcher {
    private final String apiKey;
    private final HttpClient httpClient;

    public WeatherFetcher(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(WeatherAPIConstants.DEFAULT_TIMEOUT)
                .build();
    }

    /**
     * Performs a direct HTTP call to OpenWeatherMap API.
     *
     * @param cityName name of the city
     * @return parsed WeatherData object
     */
    public WeatherData fetchWeatherFromAPI(String cityName) throws IOException, InterruptedException {
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
    }
}
