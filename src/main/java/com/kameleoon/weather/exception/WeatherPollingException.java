package com.kameleoon.weather.exception;

/**
 * Thrown when the background polling service encounters an unexpected error.
 * This exception is logged internally and should not interrupt the main flow.
 */
public class WeatherPollingException extends WeatherSdkException {
    public WeatherPollingException(String message) {
        super(message);
    }

    public WeatherPollingException(String message, Throwable cause) {
        super(message, cause);
    }
}
