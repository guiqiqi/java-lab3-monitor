В этой работе я использовал технологию многопоточности Java для создания приложения, моделирующего изменения температуры и влажности в помещении.

Переменные в среде помещения, которые необходимо контролировать:

- Температура
- Влажность

В комнате обязательно должны быть следующие предметы:

- Контроллер
- Датчик
- Вентилятор
- Обогреватель

### Рабочий процесс программы

По умолчанию окружающая среда становится влажной и холодной медленно. При этом в помещении имеется пара датчиков температуры/влажности, которые измеряют температуру/влажность в помещении через фиксированные промежутки времени.

Когда температура/влажность в помещении превышает/ниже 1% заданного значения, датчик отправит сигнал на контроллер, который будет управлять включением обогревателя/вентилятора.

При включенном вентиляторе влажность будет медленно падать; при включенном обогревателе температура будет медленно повышаться.

Более конкретно:

- Когда влажность превышает пороговое значение, включается вентилятор.
- Когда влажность упадет ниже порогового значения, вентилятор выключится.
- Когда температура превысит порог, обогреватель отключится.
- Когда температура упадет ниже порога, обогреватель включится.

### Компоненты программы

1. **Класс `Enviroment`**

    Этот класс поддерживает переменные `temprature` и `humidity` в среде для указания температуры и влажности; в то же время, чтобы поддерживать несколько потоков для одновременного изменения значений температуры и влажности, этот класс предоставляет методы класса:

    - `heating`: используется для увеличения значения `temprature` - имитация нагрева.
    - `cooling`: используется для уменьшения значения «температуры» - имитирует охлаждение.
    - `humidify`: используется для увеличения значения `humidity` - имитирует увлажнение.
    - `drying`: пользователь уменьшает значение «влажности» - имитирует осушение.

    Все вышеперечисленные методы синхронизируются через переменные блокировки `tempratureLock` и `humidityLock`, поддерживаемые внутри класса, поэтому их изменения являются потокобезопасными.

    Кроме того, класс `Enviroment` обеспечивает функцию автоматического увлажнения/охлаждения путем реализации интерфейса `Runnable` — его можно запускать как поток:

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

    Где `updateInterval` — это временной интервал для обновления влажности/температуры окружающей среды.

2. **Абстрактный класс `Sensor` и класс `EnviromentSensor`**

    Класс Sensor реализует общую реализацию датчика: он может работать как поток, реализуя интерфейс `Runnable`; при запуске он вызывает метод выборки для сбора данных - когда данные превышают/ниже порогового значения, Для выдачи уведомлений будет вызван метод `highEvent`/`lowEvent`:

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

    Все его подклассы должны реализовать следующие абстрактные методы:

       ```java
       abstract Float sampling();
       abstract void highEvent(Float current);
       abstract void lowEvent(Float current);
       ```

    Класс `EnviromentSensor` реализует функции, подобные замыканию, путем вызова соответствующего функционального интерфейса `Function<Environment, Float>` через прокси-методы `sampling` и `highEvent`/`lowEvent`:

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

3. **Абстрактный класс `Appliance` и подклассы `Fan`, `Heater`**

    Абстрактный класс `Appliance` может работать как поток, реализуя интерфейс `Runnable`; внутри него поддерживается бит флага `running` для управления работой потока:

    - Если для параметра `running` установлено значение `false`, выполнение потока будет остановлено, а блокировка будет снята с использованием метода `wait`.
    - Если для параметра `running` установлено значение `true`, поток продолжит выполнение, а в основном потоке будет вызван метод `notify`, чтобы рабочий поток мог продолжить выполнение.

    Конкретная реализация кода выглядит следующим образом:

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

    Это гарантирует, что выполнение рабочего потока может динамически контролироваться.

    Подклассы `Fan`, `Heater` реализуют операции над экземпляром связанной среды `Enviroment` путем переопределения метода `execute` - соответственно, они будут вызывать операции `heating`/`drying`, осушения и нагревания соответственно.

4. **Класс `Controller`**

    Класс `Controller` реализует динамическое управление средой путем создания экземпляров классов `Fan`, `Heater`, `EnviromentSensor` и связывания соответствующих сигналов и функций обратного вызова. В то же время этот класс также используется для вывода текущего значения датчика в консоль.

    Его метод `start` запустит все соответствующие рабочие потоки.

### Результат программы

Построив вывод программы, можно получить следующий график:

![result](https://github.com/guiqiqi/java-lab3-monitor/raw/master/resources/result.png?raw=true)

Видно, что через некоторое время температура/влажность контролируется в интервале около заданного значения – это говорит о том, что программа работает корректно.

### Тест

Я написал несколько тестов для этого проекта, которые проверяют нормальную функциональность, которую должна реализовать программа:

- `SensorTest`: используется для проверки работы сенсорного модуля.
- `ApplianceTest`: используется для проверки работы такого оборудования, как обогреватели/вентиляторы.
- `EnviromentTest`: используется для проверки работы модулей среды.

Я очищаю, компилирую и тестирую проект, используя следующие команды:

```
mvn clean
mvn compile
mvn test
```

Пройдены все тесты:

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