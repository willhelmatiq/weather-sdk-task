package com.kameleoon.weather.polling;

import com.kameleoon.weather.api.WeatherData;
import com.kameleoon.weather.api.WeatherFetcher;
import com.kameleoon.weather.cache.WeatherCache;
import com.kameleoon.weather.exception.WeatherPollingException;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background service that periodically refreshes cached weather data for stored cities.
 * Runs in a dedicated daemon thread so it does not block application shutdown.
 * Automatically updates cache entries at the specified interval to ensure
 * low-latency access in {@code WeatherMode.POLLING}.
 */
public class PollingService {

    private static final long SHUTDOWN_TIMEOUT_SECONDS = 5;
    private final Logger logger = Logger.getLogger(PollingService.class.getName());

    private final WeatherCache cache;
    private final WeatherFetcher fetcher;
    private final ScheduledExecutorService scheduler;
    private final Duration interval;
    private volatile boolean started = false;

    public PollingService(WeatherCache cache, WeatherFetcher fetcher, Duration interval, Level logLevel) {
        this.cache = cache;
        this.fetcher = fetcher;
        this.interval = interval;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "weather-polling");
            thread.setDaemon(true);
            return thread;
        });
        this.logger.setLevel(logLevel);
    }

    /** Starts periodic background refresh of cached weather data. */
    public synchronized void start() {
        if (started) return;
        started = true;
        scheduler.scheduleAtFixedRate(this::refreshCache, 0, interval.toMinutes(), TimeUnit.MINUTES);
    }

    private void refreshCache() {
        for (String city : cache.getStoredCities()) {
            try {
                WeatherData data = fetcher.fetchWeatherFromAPI(city);
                cache.put(city, data);
            } catch (Exception e) {
                WeatherPollingException pollingError = new WeatherPollingException("Polling failed for city: " + city, e);
                logger.log(Level.WARNING, "[PollingService] " + pollingError.getMessage(), pollingError);
            }
        }
    }

    /** Gracefully stops background refresh service. */
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
    }
}