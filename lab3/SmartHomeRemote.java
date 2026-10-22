class SmartHomeRemote extends Remote {
    SmartHomeRemote(String manufacturer, String model, String batteryType) {
        super(manufacturer, model, batteryType);
    }

    @Override public void turnOn() { System.out.println("Умный дом включен"); }
    @Override public void turnOff() { System.out.println("Умный дом выключен"); }
    @Override public void changeVolume(int delta) { System.out.println("В умном доме нет громкости"); }
    @Override public void changeChannel(int step) { System.out.println("В умном доме нет каналов"); }
}
