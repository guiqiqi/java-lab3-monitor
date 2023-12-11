package ru.spbstu.telematics.java;

import java.util.Random;
import java.lang.Thread;

public class Environment implements Runnable {

    private Float _temprature; // Current temprature
    private Float _humidity; // Current humidity
    private Thread workingThread; // Working thread for updating environment parameters
    private final Integer updateInterval; // Update interval for changing enviroment

    private final Object tempratureLock = new Object(); // Lock for syncornize temprature temprature
    private final Object humidityLock = new Object(); // Lock for syncornize modify of humidity

    Environment(Float initialTemprature, Float initialHumidity, Integer updateInterval) {
        this.updateInterval = updateInterval;
        this._temprature = initialTemprature;
        this._humidity = initialHumidity;
        this.workingThread = new Thread(this);
    }

    /**
     * Enviroment will try to cool and humidify itself automatically with given
     * update interval with an random value.
     * This method will be ran in a separated thread.
     */
    @Override
    public void run() {
        while (true) {
            Random random = new Random();
            this.cooling(random.nextFloat() / (1000 / updateInterval));
            this.humidify(random.nextFloat() / (1000 / updateInterval));
            try {
                Thread.sleep(this.updateInterval);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * Method for retriving current enviroment temprature.
     * 
     * @return current enviroment temprature
     */
    public Float temprature() {
        return this._temprature;
    }

    /**
     * Method for retriving current enviroment humidity.
     * 
     * @return current enviroemtn humidity
     */
    public Float humidity() {
        return this._humidity;
    }

    /**
     * Let enviroment be warmer.
     * This method will be syncornized by tempratureLock object.
     * 
     * @param delta is the delta value of temprature going to be add
     */
    void heating(Float delta) {
        synchronized (this.tempratureLock) {
            this._temprature += delta;
        }
    }

    /**
     * Let enviroment be cooler.
     * This method will be syncornized by tempratureLock object.
     * 
     * @param delta is the delta value of temprature going to be subtract
     */
    void cooling(Float delta) {
        synchronized (this.tempratureLock) {
            this._temprature -= delta;
        }
    }

    /**
     * Let enviroment be wetter.
     * This method will be syncornized by humidityLock object.
     * 
     * @param delta is the delta value of humidity going to be add
     */
    void humidify(Float delta) {
        synchronized (this.humidityLock) {
            this._humidity += delta;
        }
    }

    /**
     * Let enviroment be dryer.
     * This method will be syncornized by humidityLock object.
     * 
     * @param delta is the delta value of humidity going to be subtract
     */
    void drying(Float delta) {
        synchronized (this.humidityLock) {
            this._humidity -= delta;
        }
    }

    /**
     * Start running working thread
     */
    void boot() {
        this.workingThread.start();
    }
}
