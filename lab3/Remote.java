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

    public void showBatteryState() {
        System.out.println("Батарейки " + batteryType + ": " + batteryLevel + "%");
    }

    public int getId() { return id; }
}
