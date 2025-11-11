package com.kameleoon.weather;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Thread-safe in-memory cache for weather data.
 * - Stores up to MAX_SIZE cities.
 * - Each entry expires after TTL_SECONDS.
 * - Implements an LRU (Least Recently Used) eviction policy:
 * - Internally uses {@link ConcurrentHashMap} for thread-safe storage
 * and {@link ConcurrentLinkedDeque} to maintain access order.
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
    private final ConcurrentLinkedDeque<String> accessOrder = new ConcurrentLinkedDeque<>();

    /**
     * Adds or updates a city in cache.
     * If the limit is exceeded, removes the oldest entry.
     */
    public void put(String city, WeatherData data) {
        if (city == null || data == null) {
            return;
        }
        String key = city.toLowerCase();
        cache.put(key, new CacheEntry(data));
        updateAccessOrder(key);

        while (cache.size() > MAX_SIZE) {
            String oldest = accessOrder.pollFirst();
            if (oldest != null) {
                cache.remove(oldest);
            }
        }
    }

    /**
     * Retrieves cached weather data for the specified city.
     * - If the entry is present and not expired, it is returned and marked as recently used
     *   (moved to the end of the LRU access order).
     * - If the entry has expired, it is removed and {@code null} is returned.
     * - If the entry is not found, {@code null} is also returned.
     */
    public WeatherData get(String city) {
        if (city == null || city.isBlank()) {
            return null;
        }
        String key = city.toLowerCase();
        CacheEntry entry = cache.get(key);

        if (entry == null || entry.isExpired()) {
            cache.remove(key);
            accessOrder.remove(key);
            return null;
        }
        updateAccessOrder(key);

        return entry.data;
    }

    /**
     * Returns all currently stored city names.
     */
    public Iterable<String> getStoredCities() {
        return cache.keySet();
    }

    /**
     * Updates LRU access order for the given key.
     */
    private void updateAccessOrder(String key) {
        synchronized (accessOrder) {
            accessOrder.remove(key);
            accessOrder.addLast(key);
        }
    }
}