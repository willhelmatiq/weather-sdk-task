package com.kameleoon.weather.exception;

/**
 * Thrown when the SDK fails to parse the API response
 * into a valid {@link com.kameleoon.weather.WeatherData} object.
 */
public class WeatherParsingException extends WeatherSdkException {
    public WeatherParsingException(String message) {
        super(message);
    }

    public WeatherParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
