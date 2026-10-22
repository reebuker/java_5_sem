class TvRemote extends Remote {
    TvRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
    }

    @Override public void turnOn() { System.out.println("Телевизор включен"); }
    @Override public void turnOff() { System.out.println("Телевизор выключен"); }
    @Override public void changeVolume(int delta) { System.out.println("Громкость телевизора: " + delta); }
    @Override public void changeChannel(int step) { System.out.println("Канал телевизора: сдвиг на " + step); }
}
