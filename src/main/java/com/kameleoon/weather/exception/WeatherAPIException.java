package com.kameleoon.weather.exception;

/**
 * Thrown when an error occurs during communication with the OpenWeather API.
 */
public class WeatherAPIException extends WeatherSdkException {
    public WeatherAPIException(String message) {
        super(message);
    }

    public WeatherAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
