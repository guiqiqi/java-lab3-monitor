package ru.spbstu.telematics.java;

import java.util.function.Function;

public class Controller {
    private final String name;

    private final Appliance fan;
    private final Appliance heater;
    private final EnviromentSenser tempratureSensor;
    private final EnviromentSenser humiditySensor;

    Controller(
            String name,
            Environment environment,
            Float preferredTemprature,
            Float preferredHumidity,
            Integer samplingInterval,
            Integer executionInterval) {
        this.name = name;

        // Create appliances
        this.fan = new Fan(environment, executionInterval);
        this.heater = new Heater(environment, executionInterval);

        // Low temprature event handler
        Function<Float, Void> lowTempratureHandler = (Float temprature) -> {
            if (!this.heater.running()) {
                this.heater.start();
            }
            return null;
        };

        // High temprature event handler
        Function<Float, Void> highTempratureHandler = (Float temprature) -> {
            if (this.heater.running()) {
                this.heater.stop();
            }
            return null;
        };

        // Low humidity event handler
        Function<Float, Void> lowHumidityHandler = (Float humidity) -> {
            if (this.fan.running()) {
                this.fan.stop();
            }
            return null;
        };

        // High humidity event handler
        Function<Float, Void> highHumidityHandler = (Float humidiy) -> {
            if (!this.fan.running()) {
                this.fan.start();
            }
            return null;
        };

        // Temprature sampler
        Function<Environment, Float> tempratureSampler = (Environment env) -> {
            Float temprature = env.temprature();
            System.out.println(String.format("Temprature in %s: %f", this.name, temprature));
            return temprature;
        };

        // Humidity sampler
        Function<Environment, Float> humiditySampler = (Environment env) -> {
            Float humidity = env.humidity();
            System.out.println(String.format("Humidity in %s: %f", this.name, humidity));
            return humidity;
        };

        // Create sensors
        this.tempratureSensor = new EnviromentSenser(
                environment,
                preferredTemprature,
                samplingInterval,
                tempratureSampler,
                highTempratureHandler,
                lowTempratureHandler);
        this.humiditySensor = new EnviromentSenser(
                environment,
                preferredHumidity,
                samplingInterval,
                humiditySampler,
                highHumidityHandler,
                lowHumidityHandler);
    }

    /**
     * Start all sensors and devices.
     */
    void start() {
        this.fan.boot();
        this.heater.boot();
        this.tempratureSensor.boot();
        this.humiditySensor.boot();
    }
}
