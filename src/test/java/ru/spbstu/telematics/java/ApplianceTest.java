package ru.spbstu.telematics.java;

import org.junit.Test;
import static org.junit.Assert.*;

public class ApplianceTest {
    class TestAppliance extends Appliance {
        private Integer value;

        TestAppliance(Integer value) {
            super(100);
            this.value = value;
        }

        public void execute() {
            this.value -= 1;
        }

        public Integer getValue() {
            return this.value;
        }
    }

    @Test
    public void testNoChange() {
        TestAppliance test = new TestAppliance(100);
        test.boot();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            return;
        }
        assertEquals(test.getValue(), 100);
    }

    /**
     * method name "testStartStop" cannot be used here for no reason.
     */
    @Test
    public void testStartStopped() {
        TestAppliance test = new TestAppliance(100);
        test.boot();
        test.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            return;
        }
        test.stop();
        Integer value = test.getValue();
        assertTrue(value < 100);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            return;
        }
        assertEquals(test.getValue(), value);
    }
}
