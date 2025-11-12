package com.kameleoon.weather.exception;

/**
 * Base class for all SDK exceptions.
 */
public class WeatherSdkException extends Exception {
    public WeatherSdkException(String message) { super(message); }
    public WeatherSdkException(String message, Throwable cause) { super(message, cause); }
}
