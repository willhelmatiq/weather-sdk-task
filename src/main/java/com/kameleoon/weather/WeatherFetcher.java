package com.kameleoon.weather;

import com.google.gson.JsonParseException;
import com.kameleoon.weather.exception.WeatherAPIException;
import com.kameleoon.weather.exception.WeatherParsingException;

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
     * @param cityName Name of the city to request weather for
     * @return Parsed {@link WeatherData} object containing current weather information
     * @throws WeatherAPIException     if the API call fails or returns a non-200 response
     * @throws WeatherParsingException if JSON parsing fails
     */
    public WeatherData fetchWeatherFromAPI(String cityName) throws WeatherAPIException, WeatherParsingException {
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

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new WeatherAPIException(
                        "OpenWeather API returned status " + response.statusCode() +
                                " for city: " + cityName +
                                ". Response body: " + response.body()
                );
            }

            try {
                return WeatherData.fromJson(response.body());
            } catch (JsonParseException e) {
                throw new WeatherParsingException(
                        "Failed to parse weather data for city: " + cityName, e
                );
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherAPIException(
                    "Network or I/O error while fetching weather for city: " + cityName, e
            );
        }
    }
}
