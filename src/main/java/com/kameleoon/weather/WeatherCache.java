package com.kameleoon.weather;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Thread-safe in-memory cache for weather data.
 * - Stores up to MAX_SIZE cities.
 * - Each entry expires after TTL_SECONDS.
 * - Automatically removes expired entries on access.
 * - Evicts the oldest city (FIFO) when limit exceeded.
 */
public class WeatherCache {

    private static final int MAX_SIZE = 10;
    private static final long TTL_SECONDS = 600;

    private static class CacheEntry {
        final WeatherData data;
        final Instant timestamp;

        CacheEntry(WeatherData data) {
            this.data = data;
            this.timestamp = Instant.now();
        }

        boolean isExpired() {
            return Duration.between(timestamp, Instant.now()).toSeconds() > TTL_SECONDS;
        }
    }

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<String> order = new ConcurrentLinkedDeque<>();

    /**
     * Adds or updates a city in cache.
     * If the limit is exceeded, removes the oldest entry.
     */
    public void put(String city, WeatherData data) {
        if (city == null || data == null) {
            return;
        }
        String key = city.toLowerCase();

        if (!cache.containsKey(key)) {
            order.addLast(key);
        }

        cache.put(key, new CacheEntry(data));

        while (cache.size() > MAX_SIZE) {
            String oldest = order.pollFirst();
            if (oldest != null) {
                cache.remove(oldest);
            }
        }
    }

    /**
     * Retrieves cached data if present and not expired.
     * If expired — removes it automatically.
     */
    public WeatherData get(String city) {
        if (city == null || city.isBlank()) {
            return null;
        }
        String key = city.toLowerCase();

        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key, entry);
            order.remove(key);
            return null;
        }

        return entry.data;
    }

    /**
     * Returns all currently stored city names.
     */
    public Iterable<String> getStoredCities() {
        return cache.keySet();
    }

    /**
     * Clears the entire cache.
     */
    public void clear() {
        cache.clear();
        order.clear();
    }

    /**
     * Returns the number of entries in cache.
     */
    public int size() {
        return cache.size();
    }

    /**
     * Removes expired entries in the background (optional manual cleanup).
     */
    public void cleanup() {
        for (Map.Entry<String, CacheEntry> e : cache.entrySet()) {
            if (e.getValue().isExpired()) {
                cache.remove(e.getKey(), e.getValue());
                order.remove(e.getKey());
            }
        }
    }
}