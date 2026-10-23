/*
8.1. Предложенные методы абстрактного класса:

Включение устройства.
Выключение устройства.
Регулировка громкости.
Изменение канала.
Вывод состояния батареек пульта управления

8.2. Предложенные свойства абстрактного класса:

Производитель пульта управления.
Модель пульта управления.
Тип батареек (например, “AA” или “AAA”).
Список подключённых к пульту устройств.

8.3. Классы наследники

Пульт управления для телевизора.
Пульт управления для стереосистемы.
Пульт управления для умного дома.
*/

import java.util.List;
import java.util.ArrayList;

abstract class Remote {
    // Data
    private static int nextId = 0;
    private final int id;
    private final String manufacturer;
    private final String model;
    private final String batteryType;
    private int batteryLevel = 100;

    protected final List<Device> devices = new ArrayList<>();

    // Constructors
    protected Remote(String manufacturer, String model, String batteryType) {
        this.id = nextId++;
        this.manufacturer = manufacturer;
        this.model = model;
        this.batteryType = batteryType;
    }

    // Methods
    public abstract void turnOn();
    public abstract void turnOff();
    public abstract void changeVolume(int delta);
    public abstract void setChannel(int number);

    public int      getId() { return id; }
    public String   getManufacturer() { return manufacturer; }
    public String   getModel() { return model; }
    public String   getBatteryType() { return batteryType; }
    public List<Device> getDevices() { return List.copyOf(devices); }

    public void showBatteryState() {
        System.out.println("Батарейки " + batteryType + ": " + batteryLevel + "%");
    }

    public void showDeviceState() {
        for (Device d : devices) System.out.println(d);
    }

    public boolean isOn() {
        for (Device d : devices) {
            if (d.isOn()) return true;
        }
        return false;
    }

    public void togglePower() {
        if (isOn()) turnOff();
        else turnOn();
    }

    // Переопределяем метод родителя Object аннотацией @Override
    @Override
    public String toString() {
        return getClass().getSimpleName() + " #" + id + ": " + manufacturer + " " + model
                + ", батарейки " + batteryType;
    }
}

class TvRemote extends Remote {
    private final Tv tv = Tv.getInstance();

    TvRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
        devices.add(tv);
    }

    @Override public void turnOn() { tv.turnOn(); }
    @Override public void turnOff() { tv.turnOff(); }
    @Override public void changeVolume(int delta) { tv.changeVolume(delta); }
    @Override public void setChannel(int number) { tv.setChannel(number); }
}

class StereoRemote extends Remote {
    private final Stereo stereo = Stereo.getInstance();

    StereoRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
        devices.add(stereo);
    }

    @Override public void turnOn() { stereo.turnOn(); }
    @Override public void turnOff() { stereo.turnOff(); }
    @Override public void changeVolume(int delta) { stereo.changeVolume(delta); }
    @Override public void setChannel(int number) { stereo.setTrack(number); }
}

class SmartHomeRemote extends Remote {
    private final SmartHome home = SmartHome.getInstance();

    SmartHomeRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
        devices.add(home);
    }

    @Override public void turnOn() { home.turnOn(); }
    @Override public void turnOff() { home.turnOff(); }
    @Override public void changeVolume(int delta) { System.out.println("В умном доме нет громкости"); }
    @Override public void setChannel(int number) { System.out.println("В умном доме нет каналов"); }
}
