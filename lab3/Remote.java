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
    private String manufacturer;
    private String model;
    private String batteryType;
    private int batteryLevel = 100;

    protected final List<Device> devices = new ArrayList<>();

    // Constructors
    protected Remote(String manufacturer, String model, String batteryType) {
        this.id = nextId++;
        this.manufacturer = manufacturer;
        this.model = model;
        this.batteryType = batteryType;
    }

    protected abstract void onTurnOn();
    protected abstract void onTurnOff();
    protected abstract void onChangeVolume(int delta);
    protected abstract void onSetChannel(int number);

    public final boolean turnOn() {
        if (!canUse(false)) return false;
        onTurnOn();
        spendBattery();
        return true;
    }

    public final boolean turnOff() {
        if (!canUse(false)) return false;
        onTurnOff();
        spendBattery();
        return true;
    }

    // Setters
    public final boolean changeVolume(int delta) {
        if (!canUse(true)) return false;
        onChangeVolume(delta);
        spendBattery();
        return true;
    }

    public final boolean setChannel(int number) {
        if (!canUse(true)) return false;
        onSetChannel(number);
        spendBattery();
        return true;
    }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public void setModel(String model) { this.model = model; }
    public void setBatteryType(String batteryType) { this.batteryType = batteryType; }

    public boolean isDead() { return batteryLevel <= 0; }

    // можно ли пользоваться прямо сейчас
    public boolean checkReady() { return canUse(true); }

    private boolean canUse(boolean needsOn) {
        if (isDead()) {
            System.out.println("Пульт разряжен и больше не работает");
            return false;
        }
        if (needsOn && !isOn()) {
            System.out.println("Пульт выключен, сначала включите");
            return false;
        }
        return true;
    }

    public abstract List<String> getChannels();
    public int getBatteryLevel() { return batteryLevel; }
    public int      getId() { return id; }
    public String   getManufacturer() { return manufacturer; }
    public String   getModel() { return model; }
    public String   getBatteryType() { return batteryType; }
    public List<Device> getDevices() { return List.copyOf(devices); }

    private void spendBattery() {
        batteryLevel--;
        if (batteryLevel == 0) {
            for (Device d : devices) d.turnOff();
            System.out.println("Батарейки сели, пульт выключился навсегда");
        }
    }

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

    public boolean togglePower() { return isOn() ? turnOff() : turnOn(); }

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

    @Override protected void onTurnOn() { tv.turnOn(); }
    @Override protected void onTurnOff() { tv.turnOff(); }
    @Override protected void onChangeVolume(int delta) { tv.changeVolume(delta); }
    @Override protected void onSetChannel(int number) { tv.setChannel(number); }
    @Override public List<String> getChannels() { return tv.getChannels(); }
}

class StereoRemote extends Remote {
    private final Stereo stereo = Stereo.getInstance();

    StereoRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
        devices.add(stereo);
    }

    @Override protected void onTurnOn() { stereo.turnOn(); }
    @Override protected void onTurnOff() { stereo.turnOff(); }
    @Override protected void onChangeVolume(int delta) { stereo.changeVolume(delta); }
    @Override protected void onSetChannel(int number) { stereo.setTrack(number); }
    @Override public List<String> getChannels() { return stereo.getTracks(); }
}

class SmartHomeRemote extends Remote {
    private final SmartHome home = SmartHome.getInstance();

    SmartHomeRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
        devices.add(home);
    }

    @Override protected void onTurnOn() { home.turnOn(); }
    @Override protected void onTurnOff() { home.turnOff(); }
    @Override protected void onChangeVolume(int delta) { System.out.println("В умном доме нет громкости"); }
    @Override protected void onSetChannel(int number) { }
    @Override public List<String> getChannels() { return List.of(); }
}
