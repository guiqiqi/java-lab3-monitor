package ru.spbstu.telematics.java;

import java.util.Random;

public class Fan extends Appliance {

    private Environment environment; // Bounded enviroment

    Fan(Environment environment, Integer executioInteger) {
        super(executioInteger);
        this.environment = environment;
    }

    /**
     * Fan is going to drying the house for a random value.
     */
    @Override
    public void execute() {
        Random random = new Random();
        this.environment.drying(random.nextFloat());
    }
}
