package com.kameleoon.weather;

/**
 * Central place for API-related constants.
 */
public final class WeatherAPIConstants {
    private WeatherAPIConstants() {
        throw new AssertionError("This is a utility class and cannot be instantiated");
    }

    public static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
}
