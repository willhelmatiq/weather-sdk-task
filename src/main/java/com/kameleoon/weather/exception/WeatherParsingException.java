package com.kameleoon.weather.exception;

import com.kameleoon.weather.api.WeatherData;

/**
 * Thrown when the SDK fails to parse the API response
 * into a valid {@link WeatherData} object.
 */
public class WeatherParsingException extends WeatherSdkException {
    public WeatherParsingException(String message) {
        super(message);
    }

    public WeatherParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
