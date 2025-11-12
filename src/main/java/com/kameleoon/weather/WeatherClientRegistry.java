package com.kameleoon.weather;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry for managing WeatherApiClient instances.
 * Ensures that only one client exists per unique API key.
 */
public final class WeatherClientRegistry {

    private static final Map<String, WeatherApiClient> CLIENTS = new ConcurrentHashMap<>();

    private WeatherClientRegistry() {
        // utility class — prevent instantiation
    }

    /**
     * Returns an existing client for the given API key or creates a new one with default configuration.
     * Prevents creation of duplicate clients with the same API key.
     *
     * @param apiKey OpenWeather API key
     * @param mode   SDK mode (ON_DEMAND or POLLING)
     * @return WeatherApiClient instance
     */
    public static WeatherApiClient getClient(String apiKey, WeatherMode mode) {
        WeatherConfig defaultConfig = new WeatherConfig.Builder().build();
        return CLIENTS.computeIfAbsent(apiKey, key -> new WeatherApiClient(key, mode, defaultConfig));
    }

    /**
     * Returns an existing client for the given API key or creates a new one with default configuration.
     * Prevents creation of duplicate clients with the same API key.
     *
     * @param apiKey OpenWeather API key
     * @param mode   SDK mode (ON_DEMAND or POLLING)
     * @param config Custom SDK configuration
     * @return WeatherApiClient instance
     */
    public static WeatherApiClient getClient(String apiKey, WeatherMode mode, WeatherConfig config) {
        return CLIENTS.computeIfAbsent(apiKey, key -> new WeatherApiClient(key, mode, config));
    }

    /**
     * Deletes a client associated with the given API key.
     * Stops its background polling service (if enabled).
     *
     * @param apiKey OpenWeather API key
     */
    public static void deleteClient(String apiKey) {
        WeatherApiClient client = CLIENTS.remove(apiKey);
        if (client != null) {
            client.shutdown();
        }
    }

    /**
     * Returns all currently active clients.
     */
    public static Collection<WeatherApiClient> getAllClients() {
        return CLIENTS.values();
    }
}

