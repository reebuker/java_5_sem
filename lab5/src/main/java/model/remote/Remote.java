package model.remote;

import model.device.Device;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

abstract public class Remote {
    // Data
    private static final int MIN_DELTA_BATTERY = 2;
    private static final int MAX_DELTA_BATTERY = 5;
    private static final int MIN_START_BATTERY = 80;
    private static final int MAX_START_BATTERY = 100;

    // значения по умолчанию и правила проверки общие для меню и чтения из файла
    public static final String DEFAULT_MANUFACTURER = "Samsung";
    public static final String DEFAULT_MODEL = "Basic";
    public static final String DEFAULT_BATTERY = "AA";
    public static final int MAX_NAME_LENGTH = 20;
    public static final int MAX_BATTERY = 100;
    private static final String[] BATTERY_TYPES = { "AA", "AAA" };

    private static final int NO_ID = -1;
    private int id = NO_ID; // выдаёт RemoteRepository, когда пульт туда добавляют
    private String manufacturer;
    private String model;
    private String batteryType;

    private int batteryLevel = randomBetween(MIN_START_BATTERY, MAX_START_BATTERY); // было = 100

    protected final List<Device> devices = new ArrayList<>();

    // Constructors
    protected Remote(String manufacturer, String model, String batteryType) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.batteryType = batteryType;
    }

    protected abstract void onTurnOn();

    protected abstract void onTurnOff();

    // false, если у устройства нет громкости
    protected abstract boolean onChangeVolume(int delta);

    protected abstract void onSetChannel(int number);

    // дополнительные действия конкретного пульта: названия и их выполнение по
    // номеру с 0
    public abstract List<String> getExtraActions();

    protected abstract void onExtra(int index);

    public abstract void setChannels(List<String> items);

    // то, что у пульта меняется во время работы, по нему пишется csv
    public abstract RemoteState state();

    // применяет то, что в состоянии задано, null не меняет
    public abstract void restore(RemoteState state);

    // общая часть restore: заряд и включено ли устройство
    protected void restoreCommon(RemoteState state) {
        if (state.batteryLevel() != null)
            batteryLevel = Math.max(0, Math.min(MAX_BATTERY, state.batteryLevel()));
        if (state.on() != null) {
            for (Device d : devices) {
                if (state.on())
                    d.turnOn();
                else
                    d.turnOff();
            }
        }
        // разряженный пульт не бывает включённым
        if (isDead()) {
            for (Device d : devices)
                d.turnOff();
        }
    }

    public final ActionResult turnOn() {
        ActionResult check = check(false);
        if (check != ActionResult.OK)
            return check;
        onTurnOn();
        spendBattery();
        return ActionResult.OK;
    }

    public final ActionResult turnOff() {
        ActionResult check = check(false);
        if (check != ActionResult.OK)
            return check;
        onTurnOff();
        spendBattery();
        return ActionResult.OK;
    }

    // Setters
    public final ActionResult changeVolume(int delta) {
        ActionResult check = check(true);
        if (check != ActionResult.OK)
            return check;
        boolean hasVolume = onChangeVolume(delta);
        spendBattery();
        return hasVolume ? ActionResult.OK : ActionResult.NO_VOLUME;
    }

    public final ActionResult setChannel(int number) {
        ActionResult check = check(true);
        if (check != ActionResult.OK)
            return check;
        onSetChannel(number);
        spendBattery();
        return ActionResult.OK;
    }

    public final ActionResult runExtra(int index) {
        ActionResult check = check(true);
        if (check != ActionResult.OK)
            return check;
        onExtra(index);
        spendBattery();
        return ActionResult.OK;
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

    // второй раз id выдать нельзя
    public void assignId(int id) {
        if (this.id != NO_ID)
            throw new IllegalStateException("у пульта уже есть id " + this.id);
        this.id = id;
    }

    // AA или AAA в любом регистре (и с кириллической А), иначе null
    public static String normalizeBattery(String value) {
        if (value == null)
            return null;
        String s = value.trim().toUpperCase().replace('А', 'A'); // А - кириллическая А
        for (String t : BATTERY_TYPES) {
            if (t.equals(s))
                return t;
        }
        return null;
    }

    // случайное целое от min до max включительно
    protected static int randomBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public boolean isDead() {
        return batteryLevel <= 0;
    }

    // можно ли пользоваться прямо сейчас: OK или причина, почему нельзя
    public ActionResult checkReady() {
        return check(true);
    }

    private ActionResult check(boolean needsOn) {
        if (isDead())
            return ActionResult.REMOTE_DEAD;
        if (needsOn && !isOn())
            return ActionResult.REMOTE_OFF;
        return ActionResult.OK;
    }

    public abstract List<String> getChannels();

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

    // когда батарейки садятся, устройства выключаются, а сообщение об этом на вызывающем
    private void spendBattery() {
        batteryLevel -= randomBetween(MIN_DELTA_BATTERY, MAX_DELTA_BATTERY);
        batteryLevel = Math.max(0, batteryLevel);
        if (batteryLevel == 0) {
            for (Device d : devices)
                d.turnOff();
        }
    }

    public boolean isOn() {
        for (Device d : devices) {
            if (d.isOn())
                return true;
        }
        return false;
    }

    public ActionResult togglePower() {
        return isOn() ? turnOff() : turnOn();
    }
}
