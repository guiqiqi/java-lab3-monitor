package ru.spbstu.telematics.java;

import java.util.function.Function;

public class EnviromentSenser extends Sensor {

    private Environment environment; // Bound enviroment
    private Function<Environment, Float> samplingHandler; // Sampling method
    private Function<Float, Void> highEventHandler; // High event handler
    private Function<Float, Void> lowEventHandler; // Low event handler

    EnviromentSenser(
            Environment environment,
            Float preferred,
            Integer samplingInterval,
            Function<Environment, Float> samplingHandler,
            Function<Float, Void> highEventHandler,
            Function<Float, Void> lowEventHandler) {
        super(preferred, samplingInterval);
        this.samplingHandler = samplingHandler;
        this.highEventHandler = highEventHandler;
        this.lowEventHandler = lowEventHandler;
        this.environment = environment;
    }

    /**
     * It will call samplingHandler for reterving some data from environment.
     * Show a message to console with detected value.
     */
    @Override
    Float sampling() {
        Float value = this.samplingHandler.apply(this.environment);
        return value;
    }

    /**
     * If thershold triggered in detection, send an event to highEventHandler.
     */
    @Override
    void highEvent(Float current) {
        this.highEventHandler.apply(current);
    }

    /**
     * If detected value lower than thershold, send an event to lowEventHandler.
     */
    @Override
    void lowEvent(Float current) {
        this.lowEventHandler.apply(current);
    }
}
