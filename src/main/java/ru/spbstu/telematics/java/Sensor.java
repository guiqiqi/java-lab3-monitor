package ru.spbstu.telematics.java;

import java.lang.Thread;

public abstract class Sensor implements Runnable {
    static final Float thresholdPercentage = 0.01f; // Threshold could be dynamically adjust by this percentage,
                                                    // once sampled value higher than (1 + percentage) * threshold,
                                                    // event should be generated.
    private final Integer samplingInterval; // Sampling interval during each iteration
    private final Float preferred; // Threshold for sampled value
    private Thread workingThread; // Working thread for detection

    abstract Float sampling(); // How could senser get a value

    abstract void highEvent(Float current); // What to do when sampled value higher than threshold

    abstract void lowEvent(Float current); // What to do when sampled value lower than threshold

    Sensor(Float preferred, Integer samplingInterval) {
        this.samplingInterval = samplingInterval;
        this.preferred = preferred;
        this.workingThread = new Thread(this);
    }

    /**
     * Sensor will try to sample a value from somewhere each iteration after a while
     * (samplingInterval),
     * once we found that sampled value higher than (1 + percentage) * threshold,
     * we raise an event for let controller know that need action.
     * This method will be ran in a separated thread.
     */
    @Override
    public void run() {
        while (true) {
            Float current = this.sampling();
            if (current > preferred * (1 + Sensor.thresholdPercentage))
                this.highEvent(current);
            if (current < preferred * (1 - Sensor.thresholdPercentage))
                this.lowEvent(current);
            try {
                Thread.sleep(this.samplingInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Boot sensor - starting working thread.
     */
    public void boot() {
        this.workingThread.start();
    }
}
