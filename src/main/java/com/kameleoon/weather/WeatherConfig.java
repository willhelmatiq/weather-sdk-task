package com.kameleoon.weather;

import java.time.Duration;
import java.util.logging.Level;

/**
 * Configuration options for the Weather SDK.
 * Use {@link Builder} to customize caching, API and logging behavior.
 */
public final class WeatherConfig {
    private final int cacheSize;
    private final long cacheTtlSeconds;
    private final Duration apiTimeout;
    private final Duration pollingInterval;
    private final Level logLevel;

    private WeatherConfig(Builder builder) {
        this.cacheSize = builder.cacheSize;
        this.cacheTtlSeconds = builder.cacheTtlSeconds;
        this.apiTimeout = builder.apiTimeout;
        this.pollingInterval = builder.pollingInterval;
        this.logLevel = builder.logLevel;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public long getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public Duration getApiTimeout() {
        return apiTimeout;
    }

    public Duration getPollingInterval() {
        return pollingInterval;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    /**
     * Builder for {@link WeatherConfig}.
     */
    public static class Builder {
        private int cacheSize = 10;
        private long cacheTtlSeconds = 600;
        private Duration apiTimeout = Duration.ofSeconds(10);
        private Duration pollingInterval = Duration.ofMinutes(2);
        private Level logLevel = Level.WARNING;

        public Builder cacheSize(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("cacheSize must be positive");
            }
            this.cacheSize = size;
            return this;
        }

        public Builder cacheTtlSeconds(long seconds) {
            if (seconds <= 0) {
                throw new IllegalArgumentException("cacheTtlSeconds must be positive");
            }
            this.cacheTtlSeconds = seconds;
            return this;
        }

        public Builder apiTimeout(Duration timeout) {
            if (timeout == null || timeout.isNegative() || timeout.isZero()) {
                throw new IllegalArgumentException("apiTimeout must be positive");
            }
            this.apiTimeout = timeout;
            return this;
        }

        public Builder pollingInterval(Duration interval) {
            if (interval == null || interval.isNegative() || interval.isZero()) {
                throw new IllegalArgumentException("pollingInterval must be positive");
            }
            this.pollingInterval = interval;
            return this;
        }

        public Builder logLevel(Level level) {
            if (level == null) {
                throw new IllegalArgumentException("logLevel cannot be null");
            }
            this.logLevel = level;
            return this;
        }

        public WeatherConfig build() {
            return new WeatherConfig(this);
        }
    }
}
