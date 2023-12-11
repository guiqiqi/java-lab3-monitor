package ru.spbstu.telematics.java;

import java.lang.Thread;

public abstract class Appliance implements Runnable {
    private Boolean _running; // Flag indicates whether running or not Applicance
    private Thread workingThread; // Working thread
    protected Integer executionInterval; // Sleep interval after each execution of Applicance

    public abstract void execute(); // When Appliance going to do

    Appliance(Integer executionInterval) {
        this.executionInterval = executionInterval;
        this._running = false;
        this.workingThread = new Thread(this);
    }

    /**
     * After appliance enabled, this thread will check whether the running flag is
     * true:
     * - If so, it will execute once and then back to checking.
     * - If not, it will release lock and wait for notify.
     * 
     * Once start method of Appliance called, it will set the running flag and
     * notify thread to continue running.
     * Once stop method of Appliance called, it will unset the running flag so the
     * thread will be paused.
     */
    @Override
    public void run() {
        synchronized (this) {
            while (true) {
                if (!this._running) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                if (this._running)
                    this.execute();
                try {
                    Thread.sleep(this.executionInterval);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    /**
     * Start execution of Appliance.
     * It will try to set the running flag to true and notify,
     * so our execution thread will be able continue to work.
     */
    void start() {
        if (this._running)
            return;
        synchronized (this) {
            this._running = true;
            this.notify();
        }
    }

    /**
     * Stop execution of Appliance.
     * It will unset the running flag so in next iteration of execution,
     * thread will be paused and wait for next notify.
     */
    void stop() {
        this._running = false;
    }

    /**
     * Check if Appliance is running.
     * 
     * @return is appliance is running.
     */
    public Boolean running() {
        return this._running;
    }

    /**
     * Boot appliance - start working thread.
     */
    public void boot() {
        this.workingThread.start();
    }
}
