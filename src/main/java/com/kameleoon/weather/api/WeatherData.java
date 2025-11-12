package com.kameleoon.weather.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public record WeatherData(
        Weather weather,
        Temperature temperature,
        int visibility,
        Wind wind,
        long datetime,
        Sys sys,
        int timezone,
        String name
) {

    public record Weather(String main, String description) {}
    public record Temperature(double temp, double feels_like) {}
    public record Wind(double speed) {}
    public record Sys(long sunrise, long sunset) {}

    private static final Gson gson = new Gson();

    public static WeatherData fromJson(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        JsonObject weatherObj = root.getAsJsonArray("weather").get(0).getAsJsonObject();
        Weather weather = new Weather(
                weatherObj.get("main").getAsString(),
                weatherObj.get("description").getAsString()
        );

        JsonObject main = root.getAsJsonObject("main");
        Temperature temperature = new Temperature(
                main.get("temp").getAsDouble(),
                main.get("feels_like").getAsDouble()
        );

        JsonObject windObj = root.getAsJsonObject("wind");
        Wind wind = new Wind(
                windObj.has("speed") ? windObj.get("speed").getAsDouble() : 0.0
        );

        JsonObject sysObj = root.getAsJsonObject("sys");
        Sys sys = new Sys(
                sysObj.get("sunrise").getAsLong(),
                sysObj.get("sunset").getAsLong()
        );

        int visibility = root.has("visibility") ? root.get("visibility").getAsInt() : 0;
        long datetime = root.has("dt") ? root.get("dt").getAsLong() : 0L;
        int timezone = root.has("timezone") ? root.get("timezone").getAsInt() : 0;
        String name = root.has("name") ? root.get("name").getAsString() : "Unknown";

        return new WeatherData(weather, temperature, visibility, wind, datetime, sys, timezone, name);
    }

    public String toJson() {
        return gson.toJson(this);
    }
}
