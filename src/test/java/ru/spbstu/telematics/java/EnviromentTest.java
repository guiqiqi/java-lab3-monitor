package ru.spbstu.telematics.java;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unittest for Enviroment
 */
public class EnviromentTest {
    Environment defaultEnviroment() {
        return new Environment(10.f, 60.f, 1000);
    }

    @Test
    public void testHeating() {
        Environment environment = defaultEnviroment();
        environment.heating(10.f);
        assertEquals(environment.temprature(), 20.f);
    }

    @Test
    public void testCooling() {
        Environment environment = defaultEnviroment();
        environment.cooling(10.f);
        assertEquals(environment.temprature(), 0.f);
    }

    @Test
    public void testHumidify() {
        Environment environment = defaultEnviroment();
        environment.humidify(10.f);
        assertEquals(environment.humidity(), 70.f);
    }

    @Test
    public void testDrying() {
        Environment environment = defaultEnviroment();
        environment.drying(10.f);
        assertEquals(environment.humidity(), 50.f);
    }

    @Test
    public void testAutomaticRunning() {
        Environment environment = new Environment(10.f, 60.f, 1);
        environment.boot();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertTrue(environment.humidity() > 60.f);
        assertTrue(environment.temprature() < 10.f);
    }
}
