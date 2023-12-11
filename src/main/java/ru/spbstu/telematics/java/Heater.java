package ru.spbstu.telematics.java;

import java.util.Random;

public class Heater extends Appliance {

    private Environment environment; // Bounded environment

    Heater(Environment environment, Integer executioInteger) {
        super(executioInteger);
        this.environment = environment;
    }

    /**
     * Heater is going to heat house for a random value.
     */
    @Override
    public void execute() {
        Random random = new Random();
        this.environment.heating(random.nextFloat());
    }
}
