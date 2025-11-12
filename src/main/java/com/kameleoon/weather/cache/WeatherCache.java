package com.kameleoon.weather.cache;

import com.kameleoon.weather.WeatherData;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe in-memory cache for weather data.
 * Configurable size and TTL via {@link com.kameleoon.weather.WeatherConfig}.
 * - Implements an LRU (Least Recently Used) eviction policy:
 * - Internally uses {@link ConcurrentHashMap} for thread-safe storage
 * and {@link ConcurrentLinkedDeque} to maintain access order.
 */
public class WeatherCache {

    private final int maxSize;
    private final long ttlSeconds;

    private final ConcurrentHashMap<String, DoublyLinkedList.Node<String, WeatherData>> map = new ConcurrentHashMap<>();
    private final DoublyLinkedList<String, WeatherData> list = new DoublyLinkedList<>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public WeatherCache(int maxSize, long ttlSeconds) {
        this.maxSize = maxSize;
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * Adds or updates a city in cache.
     * If the limit is exceeded, removes the oldest entry.
     */
    public void put(String city, WeatherData data) {
        if (city == null || data == null) {
            return;
        }
        String key = city.toLowerCase();

        readWriteLock.writeLock().lock();
        try {
            var node = map.get(key);
            if (node != null) {
                node.value = data;
                node.timestampSec = Instant.now().getEpochSecond();
                list.moveToEnd(node);
            } else {
                var newNode = new DoublyLinkedList.Node<>(key, data, Instant.now().getEpochSecond());
                list.insertBeforeTail(newNode);
                map.put(key, newNode);
            }

            while (map.size() > maxSize) {
                var lru = list.first();
                if (lru == null) {
                    break;
                }
                list.unlink(lru);
                map.remove(lru.key);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Retrieves cached weather data for the specified city.
     * - If the entry is present and not expired, returns it and moves to MRU.
     * - If expired, removes it and returns null.
     */
    public WeatherData get(String city) {
        if (city == null || city.isBlank()) {
            return null;
        }

        String key = city.toLowerCase();
        DoublyLinkedList.Node<String, WeatherData> node;

        readWriteLock.readLock().lock();
        try {
            node = map.get(key);
            if (node == null) {
                return null;
            }
        } finally {
            readWriteLock.readLock().unlock();
        }

        readWriteLock.writeLock().lock();
        try {
            node = map.get(key);

            if (Instant.now().getEpochSecond() - node.timestampSec > ttlSeconds) {
                list.unlink(node);
                map.remove(key);
                return null;
            }

            list.moveToEnd(node);
            return node.value;

        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Returns all currently stored city names.
     */
    public Iterable<String> getStoredCities() {
        readWriteLock.readLock().lock();
        try {
            return map.keySet();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}