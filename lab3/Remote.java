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

abstract class Remote {
    // Data
    private static int nextId = 0;
    private final int id;
    private final String manufacturer;
    private final String model;
    private final String batteryType;
    private int batteryLevel = 100;

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
    public abstract void changeChannel(int step);

    public int      getId() { return id; }
    public String   getManufacturer() { return manufacturer; }
    public String   getModel() { return model; }
    public String   getBatteryType() { return batteryType; }

    public void showBatteryState() {
        System.out.println("Батарейки " + batteryType + ": " + batteryLevel + "%");
    }

    // Переопределяем метод родителя Object аннотацией @Override
    @Override
    public String toString() {
        return getClass().getSimpleName() + " #" + id + ": " + manufacturer + " " + model
                + ", батарейки " + batteryType;
    }
}

class TvRemote extends Remote {
    TvRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
    }

    @Override public void turnOn() { System.out.println("Телевизор включен"); }
    @Override public void turnOff() { System.out.println("Телевизор выключен"); }
    @Override public void changeVolume(int delta) { System.out.println("Громкость телевизора: " + delta); }
    @Override public void changeChannel(int step) { System.out.println("Канал телевизора: сдвиг на " + step); }
}

class StereoRemote extends Remote {
    StereoRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
    }

    @Override public void turnOn() { System.out.println("Стереосистема включена"); }
    @Override public void turnOff() { System.out.println("Стереосистема выключена"); }
    @Override public void changeVolume(int delta) { System.out.println("Громкость стереосистемы: " + delta); }
    @Override public void changeChannel(int step) { System.out.println("Трек: сдвиг на " + step); }
}

class SmartHomeRemote extends Remote {
    SmartHomeRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
    }

    @Override public void turnOn() { System.out.println("Умный дом включен"); }
    @Override public void turnOff() { System.out.println("Умный дом выключен"); }
    @Override public void changeVolume(int delta) { System.out.println("В умном доме нет громкости"); }
    @Override public void changeChannel(int step) { System.out.println("В умном доме нет каналов"); }
}
