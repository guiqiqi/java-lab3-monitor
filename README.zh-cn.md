在本次的工作中，我使用 Java 多线程技术创建了一个应用，用于模拟房间内温湿度变化的情况。

需要控制的房间内环境中的变量有：
- 温度
- 湿度

房间内需要包含的对象有：
- 控制器
- 传感器
- 风扇
- 加热器

### 程序的工作过程

默认情况下，环境会按照一个缓慢的速度变得潮湿而寒冷。与此同时，房间内有一对温度/湿度传感器，它们会按照固定的时间间隔采集房间内的温度/湿度。

当房间内的温度/湿度超过/低于预定值的 1% 时，传感器将向控制器发送信号，控制器将控制加热器/风扇打开。
当风扇打开后，湿度将缓慢下降；而当加热器打开后，温度将缓慢上升。

更具体地：

- 当湿度超过阈值时，风扇将被打开
- 当湿度低于阈值时，风扇将被关闭
- 当温度超过阈值时，加热器将被关闭
- 当温度低于阈值时，加热器将被打开

### 程序的组成部分

1. **`Enviroment` 类**

   这个类维护了环境中的 `temprature` 与 `humidity` 变量用于指示温度与湿度；同时，为了支持多个线程同时修改温度与湿度的值，该类提供了类方法：

   - `heating`：用于增加 `temprature` 的值 - 模拟加热
   - `cooling`：用于减少 `temprature` 的值 - 模拟降温
   - `humidify`: 用于增加 `humidity` 的值 - 模拟加湿
   - `drying`: 用户减少 `humidity` 的值 - 模拟除湿

   以上方法均通过类内部维护的锁变量 `tempratureLock` 与 `humidityLock` 同步，所以对它们的修改是线程安全的。

   除此之外，`Enviroment` 类通过实现 `Runnable` 接口，提供了自动加湿/降温的功能 —— 其可以被作为一个线程运行：

   ```java
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
   ```

   其中 `updateInterval` 是更新环境湿度/温度的时间间隔。

2. **`Sensor` 抽象类与 `EnviromentSensor` 类**

   `Sensor` 类实现了通用的传感器实现：它通过实现 `Runnable` 接口，可以作为一个线程运行；当运行时，它将调用 `sampling` 方法用于采集数据 —— 当数据超过/低于阈值时，将调用 `highEvent`/`lowEvent` 方法用于发出通知：

   ```java
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
   ```

   其所有子类需要实现以下抽象方法：

   ```java
   abstract Float sampling(); // How could senser get a value
   abstract void highEvent(Float current); // What to do when sampled value higher than threshold
   abstract void lowEvent(Float current); // What to do when sampled value lower than threshold
   ```

   `EnviromentSensor` 类通过代理 `sampling` 与 `highEvent`/`lowEvent` 方法，调用对应的函数接口 `Function<Environment, Float>` 实现类似闭包的功能：

   ```java
   @Override
   Float sampling() {
       Float value = this.samplingHandler.apply(this.environment);
       return value;
   }
   @Override
   void highEvent(Float current) {
       this.highEventHandler.apply(current);
   }
   @Override
   void lowEvent(Float current) {
       this.lowEventHandler.apply(current);
   }
   ```

3. **`Appliance` 抽象类与 `Fan`, `Heater` 子类**

   `Appliance` 抽象类通过实现 `Runnable` 接口，使其可以作为一个线程运行；其内部维护了一个标志位 `running` 用于控制线程运行：

   - 当 `running ` 为 `false` 时，线程的执行将被停止，并且使用 `wait` 方法释放锁
   - 当 `running` 为 `true` 时，线程将继续执行，并在主线程中调用 `notify` 方法使得工作线程得以继续执行

   具体的代码实现如下：

   ```java
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
   
   void start() {
       if (this._running)
           return;
       synchronized (this) {
           this._running = true;
           this.notify();
       }
   }
   
   void stop() {
       this._running = false;
   }
   ```

   这样保证了可以动态的控制工作线程的执行。

   `Fan`, `Heater` 子类通过重写 `execute` 方法实现了对绑定环境 `Enviroment` 实例的操作 —— 对应的，它们将分别调用 `drying`/`heating` 除湿与加热操作。

4. **`Controller` 类**

   `Controller` 类通过实例化 `Fan`, `Heater`, `EnviromentSensor` 类，并绑定对应的信号与回调函数，实现了动态地的对环境的控制功能。同时，该类还用于在 Console 中输出当前传感器的值。

   其 `start` 方法将启动所有对应的工作线程。

### 程序的结果

通过对程序输出进行绘制，得到了如下的图形：

![result](https://github.com/guiqiqi/java-lab3-monitor/raw/master/resources/result.png?raw=true)

可以看到，经过一段时候后，温度/湿度被控制设定值附近的一个区间范围内 —— 这说明我的程序工作正常。

### 测试

我为该项目编写了一些测试，它们测试了程序应该实现的正常功能：

- `SensorTest`：用于测试传感器模块的工作
- `ApplianceTest`：用于测试加热器/风扇等设备的工作
- `EnviromentTest`：用于测试环境模块的工作

通过使用下列的命令对项目进行清理、编译、测试：

```bash
mvn clean
mvn compile
mvn test
```

测试结果为全部通过：

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running ru.spbstu.telematics.java.SensorTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.232 s -- in ru.spbstu.telematics.java.SensorTest
[INFO] Running ru.spbstu.telematics.java.ApplianceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.502 s -- in ru.spbstu.telematics.java.ApplianceTest
[INFO] Running ru.spbstu.telematics.java.EnviromentTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.102 s -- in ru.spbstu.telematics.java.EnviromentTest
[INFO] Results:
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
```