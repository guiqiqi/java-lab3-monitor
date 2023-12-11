package ru.spbstu.telematics.java;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class SensorTest {
    public Sensor defaultTestSensor(Float preferred, Function<Environment, Float> sampling, Function<Float, Void> high,
            Function<Float, Void> low) {
        Environment env = new Environment(10.f, 60.f, 1000);
        Sensor sensor = new EnviromentSenser(
                env, preferred, 10, sampling, high, low);
        return sensor;
    }

    @Test
    public void testSampling() {
        Function<Environment, Float> sampling = (Environment env) -> {
            return env.humidity();
        };
        Function<Float, Void> nothing = (Float value) -> {
            return null;
        };
        Sensor sensor = defaultTestSensor(0.f, sampling, nothing, nothing);
        assertEquals(sensor.sampling(), 60.f);
    }

    @Test
    public void testHighEvent() {
        final AtomicBoolean flag = new AtomicBoolean(false);
        Function<Environment, Float> sampling = (Environment env) -> {
            return env.humidity();
        };
        Function<Float, Void> high = (Float value) -> {
            flag.set(true);
            return null;
        };
        Function<Float, Void> nothing = (Float value) -> {
            return null;
        };
        Sensor sensor = defaultTestSensor(50.f, sampling, high, nothing);
        sensor.boot();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            return;
        }
        assertEquals(flag.get(), true);
    }

    @Test
    public void testLowEvent() {
        final AtomicBoolean flag = new AtomicBoolean(false);
        Function<Environment, Float> sampling = (Environment env) -> {
            return env.temprature();
        };
        Function<Float, Void> low = (Float value) -> {
            flag.set(true);
            return null;
        };
        Function<Float, Void> nothing = (Float value) -> {
            return null;
        };
        Sensor sensor = defaultTestSensor(30.0f, sampling, nothing, low);
        sensor.boot();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            return;
        }
        assertEquals(flag.get(), true);
    }
}
