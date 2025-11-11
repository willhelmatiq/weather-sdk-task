package com.kameleoon.weather;

import java.time.Duration;

public final class WeatherAPIConstants {
    private WeatherAPIConstants() {
        throw new AssertionError("This is a utility class and cannot be instantiated");
    }

    public static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
}
