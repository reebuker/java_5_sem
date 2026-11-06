package remote;

import device.Device;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

abstract public class Remote {
    // Data
    private static final int MIN_DELTA_BATTERY = 2;
    private static final int MAX_DELTA_BATTERY = 5;
    private static final int MIN_START_BATTERY = 80;
    private static final int MAX_START_BATTERY = 100;

    private static int nextId = 0;
    private final int id;
    private String manufacturer;
    private String model;
    private String batteryType;

    private int batteryLevel = randomBetween(MIN_START_BATTERY, MAX_START_BATTERY); // было = 100

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

    // дополнительные действия конкретного пульта: названия и их выполнение по
    // номеру с 0
    public abstract List<String> getExtraActions();

    protected abstract void onExtra(int index);

    public final boolean turnOn() {
        if (!canUse(false))
            return false;
        onTurnOn();
        spendBattery();
        return true;
    }

    public final boolean turnOff() {
        if (!canUse(false))
            return false;
        onTurnOff();
        spendBattery();
        return true;
    }

    // Setters
    public final boolean changeVolume(int delta) {
        if (!canUse(true))
            return false;
        onChangeVolume(delta);
        spendBattery();
        return true;
    }

    public final boolean setChannel(int number) {
        if (!canUse(true))
            return false;
        onSetChannel(number);
        spendBattery();
        return true;
    }

    public final boolean runExtra(int index) {
        if (!canUse(true))
            return false;
        onExtra(index);
        spendBattery();
        return true;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setBatteryType(String batteryType) {
        this.batteryType = batteryType;
    }

    // случайное целое от min до max включительно
    protected static int randomBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public boolean isDead() {
        return batteryLevel <= 0;
    }

    // можно ли пользоваться прямо сейчас
    public boolean checkReady() {
        return canUse(true);
    }

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

    public String getChannelLabel() {
        return "канал / трек";
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public int getId() {
        return id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getBatteryType() {
        return batteryType;
    }

    public List<Device> getDevices() {
        return List.copyOf(devices);
    }

    private void spendBattery() {
        batteryLevel -= randomBetween(MIN_DELTA_BATTERY, MAX_DELTA_BATTERY);
        batteryLevel = Math.max(0, batteryLevel);
        if (batteryLevel == 0) {
            for (Device d : devices)
                d.turnOff();
            System.out.println("Батарейки сели, пульт выключился навсегда");
        }
    }

    public void showBatteryState() {
        System.out.println("Батарейки " + batteryType + ": " + batteryLevel + "%");
    }

    public void showDeviceState() {
        for (Device d : devices)
            System.out.println(d);
    }

    public boolean isOn() {
        for (Device d : devices) {
            if (d.isOn())
                return true;
        }
        return false;
    }

    public boolean togglePower() {
        return isOn() ? turnOff() : turnOn();
    }

    // Переопределяем метод родителя Object аннотацией @Override
    @Override
    public String toString() {
        return getClass().getSimpleName() + " #" + id + ": " + manufacturer + " " + model
                + ", батарейки " + batteryType;
    }
}
