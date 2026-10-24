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
import java.util.concurrent.ThreadLocalRandom;

abstract class Remote {
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
    
    private int batteryLevel = randomBetween(MIN_START_BATTERY, MAX_START_BATTERY);   // было = 100

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

    // дополнительные действия конкретного пульта: названия и их выполнение по номеру с 0
    public abstract List<String> getExtraActions();
    protected abstract void onExtra(int index);

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

    public final boolean runExtra(int index) {
        if (!canUse(true)) return false;
        onExtra(index);
        spendBattery();
        return true;
    }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public void setModel(String model) { this.model = model; }
    public void setBatteryType(String batteryType) { this.batteryType = batteryType; }

    // случайное целое от min до max включительно
    protected static int randomBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
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
    public String getChannelLabel() { return "канал / трек"; }
    public int getBatteryLevel() { return batteryLevel; }
    public int      getId() { return id; }
    public String   getManufacturer() { return manufacturer; }
    public String   getModel() { return model; }
    public String   getBatteryType() { return batteryType; }
    public List<Device> getDevices() { return List.copyOf(devices); }

    private void spendBattery() {
        batteryLevel -= randomBetween(MIN_DELTA_BATTERY, MAX_DELTA_BATTERY);
        batteryLevel = Math.max(0, batteryLevel);
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
    private int previousChannel = 0;   // 0 - ещё не переключали
    private int lastRandomChannel = 0; // 0 - случайный ещё не выбирали 

    TvRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
        devices.add(tv);
    }


    @Override protected void onTurnOn() { tv.turnOn(); }
    @Override protected void onTurnOff() { tv.turnOff(); }
    @Override protected void onChangeVolume(int delta) { tv.changeVolume(delta); }
    @Override protected void onSetChannel(int number) { switchTo(number); }
    @Override public List<String> getChannels() { return tv.getChannels(); }

    @Override public List<String> getExtraActions() {
        return List.of("вернуться на прошлый канал", "случайный канал");
    }

    @Override protected void onExtra(int index) {
        switch (index) {
            case 0 -> backToPrevious();
            case 1 -> randomChannel();
        }
    }

    private void switchTo(int number) {
        previousChannel = tv.getChannel();
        tv.setChannel(number);
    }

    private void backToPrevious() {
        if (previousChannel == 0) return;
        switchTo(previousChannel);
    }

    private void randomChannel() {
        lastRandomChannel = randomBetween(1, tv.getChannels().size());
        switchTo(lastRandomChannel);
    }

    @Override
    public String toString() {
        return super.toString() + ", прошлый канал: " + (previousChannel == 0 ? "нет" : previousChannel)
                + ", последний случайный: " + (lastRandomChannel == 0 ? "нет" : lastRandomChannel);
    }
}

class StereoRemote extends Remote {
    private final Stereo stereo = Stereo.getInstance();
    private int playedCount = 0;
    private int lastShuffle = 0;       

    StereoRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
        devices.add(stereo);
    }

    @Override protected void onTurnOn() { stereo.turnOn(); }
    @Override protected void onTurnOff() { stereo.turnOff(); }
    @Override protected void onChangeVolume(int delta) { stereo.changeVolume(delta); }
    @Override protected void onSetChannel(int number) { play(number); }
    @Override public List<String> getChannels() { return stereo.getTracks(); }

    @Override public List<String> getExtraActions() {
        return List.of("следующий трек", "случайный трек");
    }

    @Override protected void onExtra(int index) {
        switch (index) {
            case 0 -> nextTrack();
            case 1 -> shuffle();
        }
    }

    private void shuffle() {
        lastShuffle = randomBetween(1, stereo.getTracks().size());
        play(lastShuffle);
    }

    private void play(int number) {
        stereo.setTrack(number);
        playedCount++;
    }

    private void nextTrack() {
        play(stereo.getTrack() % stereo.getTracks().size() + 1);
    }

    @Override
    public String toString() {
        return super.toString() + ", треков включено: " + playedCount
                + ", последний случайный: " + (lastShuffle == 0 ? "нет" : lastShuffle);
    }
}

class SmartHomeRemote extends Remote {
    private final SmartHome home = SmartHome.getInstance();
    private int allOffCount = 0;
    private int lastSceneOn = -1;      // -1 - сценарий ещё не запускали   

    SmartHomeRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
        devices.add(home);
    }

    @Override protected void onTurnOn() { home.turnOn(); }
    @Override protected void onTurnOff() { home.turnOff(); }
    @Override protected void onChangeVolume(int delta) { System.out.println("В умном доме нет громкости"); }
    @Override protected void onSetChannel(int number) { home.toggleByNumber(number); }
    @Override public List<String> getChannels() { return home.getAppliances(); }
    @Override public String getChannelLabel() { return "прибор"; }

    @Override public List<String> getExtraActions() {
        return List.of("выключить все приборы", "случайный сценарий");
    }

    @Override protected void onExtra(int index) {
        switch (index) {
            case 0 -> allOff();
            case 1 -> lastSceneOn = home.randomScene();
        }
    }

    private void allOff() {
        home.allOff();
        allOffCount++;
    }

    @Override
    public String toString() {
        return super.toString() + ", «выключить всё» нажато раз: " + allOffCount
                + ", после сценария включено: " + (lastSceneOn < 0 ? "нет" : lastSceneOn);
    }
}
