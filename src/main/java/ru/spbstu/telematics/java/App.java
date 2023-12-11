package ru.spbstu.telematics.java;

public class App {
    public static void main(String[] args) {
        Environment environment = new Environment(10.f, 80.f, 500);
        Controller controller = new Controller("home", environment, 30.f, 60.f, 1000, 500);
        controller.start();
        environment.boot();
    }
}